package com.computenet.client;

import java.io.*;
import java.net.Socket;

/**
 * OriginatorClient - The simplified TCP client for web submission
 * Sends tasks to the broker from the web interface or command line
 */
public class OriginatorClient {
    
    private String brokerHost;
    private int brokerPort = 5000; // TCP port for task submission
    
    public static void main(String[] args) {
        System.out.println("Originator Client starting...");
        
        String brokerHost = args.length > 0 ? args[0] : "localhost";
        String taskData = args.length > 1 ? args[1] : "Sample task: Process data";
        
        OriginatorClient originator = new OriginatorClient(brokerHost);
        
        // Submit task
        int taskId = originator.submitTask(taskData);
        
        if (taskId > 0) {
            System.out.println("Task submitted successfully with ID: " + taskId);
        } else {
            System.err.println("Failed to submit task");
        }
    }
    
    public OriginatorClient(String brokerHost) {
        this.brokerHost = brokerHost;
    }
    
    /**
     * Submits a task to the broker via TCP
     * @param taskData The task data to submit
     * @return The task ID assigned by the broker, or -1 if failed
     */
    public int submitTask(String taskData) {
        System.out.println("Submitting task to broker: " + taskData);
        
        try (Socket socket = new Socket(brokerHost, brokerPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            // Send task data
            out.println(taskData);
            System.out.println("Task data sent to broker");
            
            // Wait for acknowledgment
            String response = in.readLine();
            System.out.println("Broker response: " + response);
            
            // Parse task ID from response
            if (response != null && response.startsWith("TASK_ACCEPTED:")) {
                String[] parts = response.split(":");
                if (parts.length >= 2) {
                    return Integer.parseInt(parts[1]);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error submitting task: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Interactive mode for submitting multiple tasks
     */
    public void interactiveMode() {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("\n=== Originator Client - Interactive Mode ===");
        System.out.println("Enter task data (or 'exit' to quit):");
        
        try {
            while (true) {
                System.out.print("> ");
                String taskData = consoleReader.readLine();
                
                if (taskData == null || taskData.equalsIgnoreCase("exit")) {
                    break;
                }
                
                if (!taskData.trim().isEmpty()) {
                    int taskId = submitTask(taskData);
                    if (taskId > 0) {
                        System.out.println("✓ Task submitted with ID: " + taskId);
                    } else {
                        System.err.println("✗ Failed to submit task");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Console error: " + e.getMessage());
        }
        
        System.out.println("Exiting...");
    }
}
