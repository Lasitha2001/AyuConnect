package com.computenet.broker.server;

import com.computenet.broker.service.TaskManager;
import com.computenet.broker.service.TaskSubmissionHandler;
import com.computenet.broker.service.TaskConfigMulticaster;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * M1: Task Submission TCP Handler (Blocking)
 * Receives tasks from originators via TCP and hands them off to the executor service
 * M2: Uses TaskSubmissionHandler to split tasks into sub-tasks
 * M4: Integrates multicast task configuration broadcasting
 */
public class TaskTcpReceiver implements Runnable {
    private ServerSocket serverSocket;
    private final int tcpPort = 5000;
    private final TaskManager taskManager;
    private final ExecutorService tcpTaskExecutor;
    private final TaskSubmissionHandler taskSubmissionHandler;
    private final WorkerNIOHandler nioHandler; // M3: NIO handler for broadcasts

    public TaskTcpReceiver(TaskManager taskManager, ExecutorService tcpTaskExecutor, 
                           WorkerNIOHandler nioHandler, TaskConfigMulticaster taskConfigMulticaster) {
        this.taskManager = taskManager;
        this.tcpTaskExecutor = tcpTaskExecutor;
        this.nioHandler = nioHandler;
        this.taskSubmissionHandler = new TaskSubmissionHandler(taskManager, tcpTaskExecutor, 
                                                                nioHandler, taskConfigMulticaster);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(tcpPort);
            System.out.println("M1: TCP Task Receiver listening on port " + tcpPort);
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept(); // BLOCKING CALL
                System.out.println("M1: Originator connected. Handing off to Executor...");
                // M2's Executor is used here to process the reliable submission
                tcpTaskExecutor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("M1 TCP Receiver error: " + e.getMessage());
        }
    }

    /**
     * Handles client connection and task submission
     * Uses BufferedReader/PrintWriter for reliable TCP communication
     * M2: After accepting task, uses TaskSubmissionHandler to split and dispatch
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            // M1: Read task data from client
            String taskData = in.readLine();
            System.out.println("M1: Received task data: " + taskData);
            
            // Parse task data to extract task name and sub-task count
            // Format: "TaskID:xxx | Name:xxx | Data:xxx | SubTasks:n"
            String taskName = "Unnamed Task";
            String actualData = taskData;
            int subTaskCount = taskManager.getAvailableWorkers().size();
            
            if (taskData != null && taskData.contains("|")) {
                String[] parts = taskData.split("\\|");
                for (String part : parts) {
                    part = part.trim();
                    if (part.startsWith("Name:")) {
                        taskName = part.substring(5).trim();
                    } else if (part.startsWith("Data:")) {
                        actualData = part.substring(5).trim();
                    } else if (part.startsWith("SubTasks:")) {
                        try {
                            subTaskCount = Integer.parseInt(part.substring(9).trim());
                        } catch (NumberFormatException e) {
                            System.err.println("M1: Invalid sub-task count, using default");
                        }
                    }
                }
            }
            
            // M1: Create task in TaskManager with custom parameters
            int taskId = taskManager.createTask(taskName, actualData, subTaskCount);
            
            // M1: Send acknowledgment back to client
            out.println("TASK_ACCEPTED:" + taskId);
            System.out.println("M1: Task '" + taskName + "' accepted with ID: " + taskId);
            
            // M2: Process task using multi-threading (split into N sub-tasks and dispatch to workers)
            taskSubmissionHandler.processTask(taskId, taskName, actualData, subTaskCount);
            
        } catch (IOException e) {
            System.err.println("M1: Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("M1: Error closing client socket: " + e.getMessage());
            }
        }
    }
}