package com.computenet.broker.service;

import com.computenet.broker.server.WorkerNIOHandler;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * M2: TaskSubmissionHandler - Multi-threaded Task Processing
 * Splits incoming tasks into sub-tasks and dispatches them to registered workers
 * using ExecutorService for concurrent processing.
 * M3: Notifies NIO handler when processing new tasks for broadcasts
 * M4: Broadcasts task configuration via multicast before dispatch
 */
public class TaskSubmissionHandler {
    
    private final TaskManager taskManager;
    private final ExecutorService executorService;
    private final WorkerNIOHandler nioHandler; // M3: NIO handler for broadcasts
    private final TaskConfigMulticaster taskConfigMulticaster; // M4: Multicast broadcaster
    
    public TaskSubmissionHandler(TaskManager taskManager, ExecutorService executorService, 
                                 WorkerNIOHandler nioHandler, TaskConfigMulticaster taskConfigMulticaster) {
        this.taskManager = taskManager;
        this.executorService = executorService;
        this.nioHandler = nioHandler;
        this.taskConfigMulticaster = taskConfigMulticaster;
    }
    
    /**
     * M2: Processes a task by splitting it into N sub-tasks and dispatching to workers
     * Uses ExecutorService to create concurrent threads
     * M3: Sets current task ID for NIO broadcasts
     * M4: Broadcasts task configuration via multicast BEFORE dispatch
     * 
     * @param taskId The ID of the task to process
     * @param taskName The name of the task
     * @param taskData The original task data
     * @param subTaskCount Number of sub-tasks to create
     */
    public void processTask(int taskId, String taskName, String taskData, int subTaskCount) {
        System.out.println("\n========================================");
        System.out.println("M2: Starting multi-threaded task processing");
        System.out.println("M2: Task ID: " + taskId);
        System.out.println("M2: Task Name: " + taskName);
        System.out.println("M2: Task Data: " + taskData);
        System.out.println("M2: Sub-tasks to create: " + subTaskCount);
        System.out.println("========================================");
        
        // M3: Set current task for NIO broadcasts
        if (nioHandler != null) {
            nioHandler.setCurrentTask(taskId);
            System.out.println("M3: NIO handler notified of new task " + taskId);
        }
        
        // Get available workers
        List<TaskManager.WorkerDetails> workers = taskManager.getAvailableWorkers();
        
        if (workers.isEmpty()) {
            System.err.println("M2: ERROR - No workers registered! Cannot process task.");
            return;
        }
        
        // Validate sub-task count
        int actualSubTasks = Math.min(subTaskCount, workers.size());
        if (actualSubTasks < subTaskCount) {
            System.out.println("M2: WARNING - Requested " + subTaskCount + " sub-tasks but only " + 
                             workers.size() + " workers available. Using " + actualSubTasks + " sub-tasks.");
        }
        
        // Split task into N sub-tasks FIRST (needed for M4 broadcast)
        List<String> subTasks = splitTaskIntoSubTasks(taskData, actualSubTasks);
        
        // M4: Broadcast task configuration via multicast BEFORE dispatching sub-tasks
        // Now includes task name and all sub-task data
        if (taskConfigMulticaster != null) {
            boolean broadcastSuccess = taskConfigMulticaster.broadcastTaskConfig(
                String.valueOf(taskId),
                taskName,
                actualSubTasks,
                taskData,
                subTasks
            );
            
            if (broadcastSuccess) {
                System.out.println("M4: Task configuration broadcasted successfully (with " + 
                                 subTasks.size() + " sub-tasks)");
                // Wait 100ms for workers to receive multicast config
                try {
                    Thread.sleep(100);
                    System.out.println("M4: Wait complete - workers ready");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("M4: Wait interrupted: " + e.getMessage());
                }
            } else {
                System.err.println("M4: WARNING - Multicast broadcast failed, proceeding with dispatch anyway");
            }
        }
        
        // Create Callable tasks for each sub-task (round-robin distribution)
        List<Future<String>> futures = new ArrayList<>();
        
        for (int i = 0; i < actualSubTasks; i++) {
            final int subTaskId = i + 1;
            final String subTaskData = subTasks.get(i);
            final TaskManager.WorkerDetails worker = workers.get(i % workers.size()); // Round-robin
            
            // Track sub-task assignment to worker
            String workerKey = worker.address() + ":" + worker.tcpPort();
            taskManager.assignSubTaskToWorker(workerKey, taskId, subTaskId);
            
            // M2: Create a Callable that will dispatch sub-task to worker via TCP
            Callable<String> subTaskCallable = new SubTaskDispatcher(
                taskId, 
                subTaskId, 
                subTaskData, 
                worker,
                taskManager
            );
            
            // M2: Submit to ExecutorService for concurrent execution
            Future<String> future = executorService.submit(subTaskCallable);
            futures.add(future);
            
            System.out.println("M2: Thread " + subTaskId + " created for sub-task dispatch to worker " + 
                             worker.address() + ":" + worker.tcpPort());
        }
        
        System.out.println("M2: All " + futures.size() + " sub-tasks submitted to ExecutorService");
        System.out.println("========================================\n");
        
        // Optional: Wait for all sub-tasks to complete (in a real system, this would be async)
        // For demonstration, we'll let them run independently
    }
    
    /**
     * Legacy method for backward compatibility
     */
    @Deprecated
    public void processTask(int taskId, String taskData) {
        processTask(taskId, "Unnamed Task", taskData, 5);
    }
    
    /**
     * Splits the main task into 5 sub-tasks
     * Each sub-task represents a portion of the work
     */
    private List<String> splitTaskIntoSubTasks(String taskData, int numSubTasks) {
        List<String> subTasks = new ArrayList<>();
        
        System.out.println("M2: Splitting task into " + numSubTasks + " sub-tasks...");
        
        for (int i = 1; i <= numSubTasks; i++) {
            // Create sub-task with portion identifier
            String subTask = String.format("SubTask-%d/%d: %s [Partition %d]", 
                i, numSubTasks, taskData, i);
            subTasks.add(subTask);
            System.out.println("M2:   - Sub-task " + i + " created");
        }
        
        return subTasks;
    }
    
    /**
     * M2: Callable implementation for dispatching sub-tasks to workers via TCP
     * Each instance runs in a separate thread from the ExecutorService
     */
    private static class SubTaskDispatcher implements Callable<String> {
        
        private final int taskId;
        private final int subTaskId;
        private final String subTaskData;
        private final TaskManager.WorkerDetails worker;
        
        public SubTaskDispatcher(int taskId, int subTaskId, String subTaskData, 
                                TaskManager.WorkerDetails worker, TaskManager taskManager) {
            this.taskId = taskId;
            this.subTaskId = subTaskId;
            this.subTaskData = subTaskData;
            this.worker = worker;
        }
        
        @Override
        public String call() throws Exception {
            String threadName = Thread.currentThread().getName();
            System.out.println("M2: [Thread " + threadName + "] Dispatching sub-task " + subTaskId + 
                             " to worker " + worker.address() + ":" + worker.tcpPort());
            
            // M2: Establish NEW blocking TCP connection to worker for reliable sub-task dispatch
            try (Socket workerSocket = new Socket(worker.address(), worker.tcpPort());
                 PrintWriter out = new PrintWriter(workerSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()))) {
                
                // Send sub-task data to worker
                String message = "TASK:" + taskId + ":SUBTASK:" + subTaskId + ":" + subTaskData;
                out.println(message);
                
                System.out.println("M2: [Thread " + threadName + "] Sub-task " + subTaskId + " sent to worker");
                
                // Wait for worker acknowledgment
                String response = in.readLine();
                
                if (response != null && response.startsWith("ACK")) {
                    System.out.println("M2: [Thread " + threadName + "] Sub-task " + subTaskId + 
                                     " acknowledged by worker: " + response);
                    
                    // Note: Task completion is tracked when worker sends completion notification
                    // via /api/worker-complete endpoint, not here at dispatch time
                    
                    return "SUCCESS: Sub-task " + subTaskId + " dispatched";
                } else {
                    System.err.println("M2: [Thread " + threadName + "] Sub-task " + subTaskId + 
                                     " - Invalid response from worker: " + response);
                    return "FAILED: Sub-task " + subTaskId;
                }
                
            } catch (IOException e) {
                System.err.println("M2: [Thread " + threadName + "] ERROR dispatching sub-task " + 
                                 subTaskId + ": " + e.getMessage());
                return "ERROR: " + e.getMessage();
            }
        }
    }
}
