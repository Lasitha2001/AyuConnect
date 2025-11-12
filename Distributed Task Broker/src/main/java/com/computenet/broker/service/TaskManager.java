package com.computenet.broker.service;

import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TaskManager: Manages all in-memory data structures for the Broker Server.
 * This includes the pool of active workers and the state of the current task.
 */
public class TaskManager {

    // --- In-Memory Data Structures ---

    // M5: Stores active workers registered via UDP/TCP. Key: Worker IP:Port, Value: Worker details.
    // ConcurrentHashMap ensures thread safety for registration/deregistration.
    private final Map<String, WorkerDetails> workerPool = new ConcurrentHashMap<>();

    // M3/M4: Stores non-blocking channels for the NIO broadcast engine.
    // Key: SocketChannel object, Value: Worker Key (IP:Port)
    private final Map<SocketChannel, String> nioWorkerChannels = new ConcurrentHashMap<>();

    // Stores the state of the current distributed task.
    // Key: Task ID, Value: Task State Object (details below)
    private final Map<Integer, TaskState> activeTasks = new ConcurrentHashMap<>();
    
    // Track sub-task assignments per worker (for worker statistics)
    // Key: Worker Key (IP:Port), Value: Set of sub-task keys (taskId-subTaskId)
    private final Map<String, Set<String>> workerSubTasks = new ConcurrentHashMap<>();

    // Used to generate unique IDs for new tasks. Thread-safe counter.
    private final AtomicInteger taskIdGenerator = new AtomicInteger(1000);

    // --- Nested Classes for Data Modeling ---

    /** Simple record to hold details about an active Worker Client. */
    public record WorkerDetails(String address, int tcpPort, String status) {}

    /** State object for tracking a task being processed across the network. */
    public record TaskState(
            String taskName,
            String originalTaskData,
            int totalSubTasks,
            AtomicInteger completedSubTasks,
            Map<Integer, String> results
    ) {}


    // --- Core Methods for Network Component Interaction ---

    // M5: Called by WorkerUdpListener to register a new worker.
    public void registerWorker(String ipAddress, int tcpPort) {
        String key = ipAddress + ":" + tcpPort;
        workerPool.put(key, new WorkerDetails(ipAddress, tcpPort, "IDLE"));
        System.out.println("TaskManager: Worker registered in memory: " + key);
    }

    // M3: Called by WorkerNIOHandler to register a channel for broadcasting.
    public void registerNIOChannel(SocketChannel channel, String workerKey) {
        nioWorkerChannels.put(channel, workerKey);
        System.out.println("TaskManager: NIO channel registered for broadcast.");
    }

    // M1: Called by TaskTcpReceiver when a new task is reliably submitted.
    public int createTask(String taskName, String taskData, int subTaskCount) {
        int newId = taskIdGenerator.incrementAndGet();
        
        // Validate sub-task count
        int numWorkers = !workerPool.isEmpty() ? workerPool.size() : 1;
        int actualSubTasks = Math.min(subTaskCount, numWorkers);
        
        if (actualSubTasks < subTaskCount) {
            System.out.println("TaskManager: Requested " + subTaskCount + " sub-tasks, but only " + 
                             numWorkers + " workers available. Using " + actualSubTasks + " sub-tasks.");
        }

        TaskState newState = new TaskState(
            taskName,
            taskData,
            actualSubTasks,
            new AtomicInteger(0),
            new ConcurrentHashMap<>()
        );
        activeTasks.put(newId, newState);
        System.out.println("TaskManager: New Task '" + taskName + "' created with ID: " + newId + 
                         ". Split into " + actualSubTasks + " sub-tasks.");
        return newId;
    }
    
    // Legacy method for backward compatibility (auto-calculate sub-tasks)
    @Deprecated
    public int createTask(String taskData) {
        int numWorkers = !workerPool.isEmpty() ? workerPool.size() : 1;
        return createTask("Unnamed Task", taskData, numWorkers);
    }

    // M2: Called by the ExecutorService thread when a sub-task result returns.
    public void submitSubTaskResult(int taskId, int subTaskId, String result) {
        TaskState state = activeTasks.get(taskId);
        if (state != null) {
            state.completedSubTasks().incrementAndGet();
            state.results().put(subTaskId, result);
            System.out.println("TaskManager: Sub-task " + subTaskId + " completed.");
        }
    }

    // M3: Provides the current progress for the NIO broadcast.
    public String getTaskProgress(int taskId) {
        TaskState state = activeTasks.get(taskId);
        if (state == null) return "No active task";
        
        int completed = state.completedSubTasks().get();
        int total = state.totalSubTasks();
        int percentage = total > 0 ? (completed * 100) / total : 0;
        
        return String.format("%d%% (%d/%d)", percentage, completed, total);
    }
    
    // Getter for M2 to know which workers are available for dispatch.
    public List<WorkerDetails> getAvailableWorkers() {
        // Return a synchronized, unmodifiable view of the workers
        return Collections.unmodifiableList(workerPool.values().stream().toList());
    }
    
    // Getter for NIO worker channels
    public Map<SocketChannel, String> getNIOWorkerChannels() {
        return Collections.unmodifiableMap(nioWorkerChannels);
    }
    
    // M2: Track when a sub-task is assigned to a worker
    public void assignSubTaskToWorker(String workerKey, int taskId, int subTaskId) {
        String subTaskKey = taskId + "-" + subTaskId;
        workerSubTasks.computeIfAbsent(workerKey, k -> ConcurrentHashMap.newKeySet()).add(subTaskKey);
        System.out.println("TaskManager: Assigned sub-task " + subTaskKey + " to worker " + workerKey);
    }
    
    // M2: Track when a sub-task is completed by a worker
    public void completeSubTaskForWorker(String workerKey, int taskId, int subTaskId) {
        String subTaskKey = taskId + "-" + subTaskId;
        Set<String> tasks = workerSubTasks.get(workerKey);
        if (tasks != null) {
            tasks.remove(subTaskKey);
            System.out.println("TaskManager: Removed completed sub-task " + subTaskKey + " from worker " + workerKey);
        }
    }
    
    // Get number of pending sub-tasks for a worker
    public int getWorkerPendingSubTasks(String workerKey) {
        Set<String> tasks = workerSubTasks.get(workerKey);
        return tasks != null ? tasks.size() : 0;
    }
    
    // Get worker statistics for all workers
    public Map<String, WorkerStats> getWorkerStatistics() {
        Map<String, WorkerStats> stats = new ConcurrentHashMap<>();
        for (Map.Entry<String, WorkerDetails> entry : workerPool.entrySet()) {
            String workerKey = entry.getKey();
            WorkerDetails details = entry.getValue();
            int pendingTasks = getWorkerPendingSubTasks(workerKey);
            
            String status;
            if (pendingTasks == 0) {
                status = "IDLE";
            } else if (pendingTasks == 1) {
                status = "ACTIVE";
            } else {
                status = "BUSY";
            }
            
            stats.put(workerKey, new WorkerStats(
                details.address(),
                details.tcpPort(),
                status,
                pendingTasks
            ));
        }
        return stats;
    }
    
    // Get task overview with progress
    public Map<String, Object> getTaskOverview() {
        Map<String, Object> overview = new ConcurrentHashMap<>();
        List<TaskInfo> pendingTasks = new java.util.ArrayList<>();
        List<TaskInfo> completedTasks = new java.util.ArrayList<>();
        
        for (Map.Entry<Integer, TaskState> entry : activeTasks.entrySet()) {
            int taskId = entry.getKey();
            TaskState state = entry.getValue();
            int completed = state.completedSubTasks().get();
            int total = state.totalSubTasks();
            
            TaskInfo taskInfo = new TaskInfo(
                taskId,
                state.taskName(),
                completed,
                total,
                completed == total ? "COMPLETED" : "PENDING"
            );
            
            if (completed == total) {
                completedTasks.add(taskInfo);
            } else {
                pendingTasks.add(taskInfo);
            }
        }
        
        overview.put("pendingTasks", pendingTasks);
        overview.put("completedTasks", completedTasks);
        overview.put("totalPending", pendingTasks.size());
        overview.put("totalCompleted", completedTasks.size());
        
        return overview;
    }
    
    // Record to hold worker statistics
    public record WorkerStats(String address, int tcpPort, String status, int pendingSubTasks) {}
    
    // Record to hold task information
    public record TaskInfo(int taskId, String taskName, int completedSubTasks, int totalSubTasks, String status) {}
}
