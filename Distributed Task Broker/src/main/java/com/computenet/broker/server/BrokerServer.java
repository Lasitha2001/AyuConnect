package com.computenet.broker.server;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.channels.Selector;
import com.computenet.broker.service.TaskManager;
import com.computenet.broker.service.TaskConfigMulticaster;
import com.computenet.client.OriginatorClient;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import java.util.Map;

/**
 * BrokerServer - Main server class
 * Coordinates all components: TCP Task Receiver, NIO Handler, UDP Listener, HTTP Data Loader, and Web UI
 * M1: TCP Task Receiver
 * M2: Multi-threaded Task Processing (ExecutorService + Worker HTTP Interfaces)
 * M3: NIO Selector
 * M4: Multicast Task Configuration Broadcaster
 * M5: UDP Listener
 */
public class BrokerServer {
    private final ExecutorService tcpTaskExecutor = Executors.newFixedThreadPool(10); // M2: Multi-threading
    private final Selector nioSelector;
    private final TaskManager taskManager = new TaskManager();
    private final TaskConfigMulticaster taskConfigMulticaster = new TaskConfigMulticaster(); // M4: Multicast Task Config
    private WorkerNIOHandler nioHandler; // M3: NIO Handler reference
    private Javalin app;
    
    // Store WebSocket connections for broadcasting M3 NIO updates
    private final Map<String, WsContext> wsClients = new ConcurrentHashMap<>();

    public BrokerServer() throws IOException {
        this.nioSelector = Selector.open(); // M3: Java NIO
    }

    public void start() throws Exception {
        // M1: Start the blocking TCP listener in a separate thread
        Thread tcpListenerThread = new Thread(new TaskTcpReceiver(taskManager, tcpTaskExecutor, getNioHandler(), taskConfigMulticaster)); 
        tcpListenerThread.start();

        // M3: Start the single-thread NIO broadcast engine
        nioHandler = new WorkerNIOHandler(nioSelector, taskManager);
        Thread nioBroadcastThread = new Thread(nioHandler);
        nioBroadcastThread.start();

        // M5: Start the lightweight UDP listener for worker registration
        Thread udpListenerThread = new Thread(new WorkerUdpListener(taskManager));
        udpListenerThread.start();

        // Start the Javalin Web Server for the UI
        app = Javalin.create(config -> {
            // Configure static file serving from resources/public
            config.staticFiles.add("/public");
            config.http.prefer405over404 = true;
        }).start(8080);
        
        // WebSocket endpoint for real-time dashboard updates
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                String clientId = String.valueOf(ctx.hashCode()); // Use context hashcode as unique ID
                wsClients.put(clientId, ctx);
                System.out.println("WebSocket client connected: " + ctx.session.getRemoteAddress() + " (ID: " + clientId + ")");
            });
            
            ws.onMessage(ctx -> {
                String message = ctx.message();
                System.out.println("WebSocket message received: " + message);
                
                // Handle different message types
                if (message.contains("REQUEST_STATUS")) {
                    // Send status update to client
                    String statusJson = String.format(
                        "{\"type\":\"STATUS_UPDATE\",\"data\":{\"activeWorkers\":%d,\"pendingTasks\":0,\"completedTasks\":0}}",
                        taskManager.getAvailableWorkers().size()
                    );
                    ctx.send(statusJson);
                }
            });
            
            ws.onClose(ctx -> {
                String clientId = String.valueOf(ctx.hashCode());
                wsClients.remove(clientId);
                System.out.println("WebSocket client disconnected: " + ctx.session.getRemoteAddress() + " (ID: " + clientId + ")");
            });
        });
        
        // REST endpoint to get worker status
        app.get("/api/workers", ctx -> {
            ctx.json(taskManager.getAvailableWorkers());
        });
        
        // REST endpoint to get worker statistics with sub-task counts
        app.get("/api/worker-stats", ctx -> {
            ctx.json(taskManager.getWorkerStatistics());
        });
        
        // REST endpoint to get task overview (pending and completed tasks)
        app.get("/api/task-overview", ctx -> {
            ctx.json(taskManager.getTaskOverview());
        });
        
        // M2: REST endpoint to receive sub-task completion notification from workers
        app.post("/api/worker-complete", ctx -> {
            try {
                String body = ctx.body();
                String workerAddress = extractJsonValue(body, "workerAddress");
                String workerPort = extractJsonValue(body, "workerPort");
                String taskId = extractJsonValue(body, "taskId");
                String subTaskId = extractJsonValue(body, "subTaskId");
                String result = extractJsonValue(body, "result");
                
                String workerKey = workerAddress + ":" + workerPort;
                int tid = Integer.parseInt(taskId);
                int sid = Integer.parseInt(subTaskId);
                
                // Update TaskManager: increment task completion counter and remove from worker's pending list
                taskManager.submitSubTaskResult(tid, sid, result != null ? result : "Completed");
                taskManager.completeSubTaskForWorker(workerKey, tid, sid);
                
                System.out.println("BrokerServer: Received completion notification from " + workerKey + 
                                   " for sub-task " + tid + "-" + sid);
                
                ctx.json(Map.of(
                    "success", true,
                    "message", "Sub-task completion recorded"
                ));
            } catch (Exception e) {
                System.err.println("BrokerServer: Error processing completion notification: " + e.getMessage());
                ctx.status(400).json(Map.of(
                    "success", false,
                    "message", "Invalid completion data: " + e.getMessage()
                ));
            }
        });
        
        // M1: REST endpoint to submit task via HTTP POST (triggers TCP submission)
        app.post("/api/submit-task", ctx -> {
            try {
                // Parse request body
                String body = ctx.body();
                System.out.println("Received task submission request: " + body);
                
                // Extract fields from JSON
                String taskId = extractJsonValue(body, "taskId");
                String taskName = extractJsonValue(body, "taskName");
                String taskData = extractJsonValue(body, "taskData");
                String subTaskCountStr = extractJsonValue(body, "subTaskCount");
                
                // Validate required fields
                if (taskId == null || taskId.isEmpty()) {
                    taskId = "AUTO_" + System.currentTimeMillis();
                }
                
                if (taskName == null || taskName.isEmpty()) {
                    ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Task name is required"
                    ));
                    return;
                }
                
                if (taskData == null || taskData.isEmpty()) {
                    ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Task data is required"
                    ));
                    return;
                }
                
                int subTaskCount = 1;
                try {
                    subTaskCount = Integer.parseInt(subTaskCountStr);
                } catch (NumberFormatException e) {
                    ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Invalid sub-task count"
                    ));
                    return;
                }
                
                // Validate sub-task count against worker count
                int workerCount = taskManager.getAvailableWorkers().size();
                if (workerCount == 0) {
                    ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "No workers registered. Start workers first."
                    ));
                    return;
                }
                
                if (subTaskCount > workerCount) {
                    ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Sub-task count (" + subTaskCount + ") exceeds registered workers (" + workerCount + ")"
                    ));
                    return;
                }
                
                if (subTaskCount < 1) {
                    ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Sub-task count must be at least 1"
                    ));
                    return;
                }
                
                // Create task message for TCP submission
                String fullTaskData = "TaskID:" + taskId + " | Name:" + taskName + " | Data:" + taskData + " | SubTasks:" + subTaskCount;
                
                // Submit task via M1 TCP using OriginatorClient
                OriginatorClient originator = new OriginatorClient("localhost");
                int assignedTaskId = originator.submitTask(fullTaskData);
                
                if (assignedTaskId > 0) {
                    ctx.json(Map.of(
                        "success", true,
                        "message", "Task '" + taskName + "' submitted successfully via TCP",
                        "taskId", assignedTaskId,
                        "originalTaskId", taskId,
                        "taskName", taskName,
                        "subTaskCount", subTaskCount
                    ));
                } else {
                    ctx.status(500).json(Map.of(
                        "success", false,
                        "message", "Failed to submit task to broker"
                    ));
                }
            } catch (Exception e) {
                ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Error submitting task: " + e.getMessage()
                ));
            }
        });
        
        System.out.println("Broker Server started successfully!");
        System.out.println("  TCP Task Receiver: port 5000");
        System.out.println("  UDP Worker Listener: port 5001");
        System.out.println("  NIO Broadcast Handler: port 5002");
        System.out.println("  Multicast Task Config: " + TaskConfigMulticaster.getMulticastAddress() + ":" + TaskConfigMulticaster.getMulticastPort());
        System.out.println("  Web UI: http://localhost:8080");
        System.out.println("  WebSocket: ws://localhost:8080/ws");
    }
    
    public void stop() {
        if (app != null) {
            app.stop();
        }
        tcpTaskExecutor.shutdown();
        System.out.println("Broker Server stopped");
    }
    
    /**
     * M3: Returns the NIO handler for task progress updates
     */
    public WorkerNIOHandler getNioHandler() {
        return nioHandler;
    }
    
    /**
     * Broadcast M3 NIO progress updates to all connected WebSocket clients
     */
    public void broadcastNIOUpdate(String message) {
        String broadcastJson = String.format(
            "{\"type\":\"NIO_BROADCAST\",\"message\":\"%s\",\"timestamp\":%d}",
            message.replace("\"", "\\\""),
            System.currentTimeMillis()
        );
        
        wsClients.values().forEach(ctx -> {
            try {
                ctx.send(broadcastJson);
            } catch (Exception e) {
                System.err.println("Error broadcasting to WebSocket client: " + e.getMessage());
            }
        });
    }
    
    /**
     * Helper method to extract JSON values (simple parsing)
     */
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return null;
            
            startIndex += searchKey.length();
            // Skip whitespace
            while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
                startIndex++;
            }
            
            // Check if value is quoted
            if (json.charAt(startIndex) == '"') {
                startIndex++; // Skip opening quote
                int endIndex = json.indexOf('"', startIndex);
                if (endIndex == -1) return null;
                return json.substring(startIndex, endIndex);
            } else {
                // Unquoted value (number, boolean, etc.)
                int endIndex = startIndex;
                while (endIndex < json.length() && 
                       json.charAt(endIndex) != ',' && 
                       json.charAt(endIndex) != '}' &&
                       json.charAt(endIndex) != ']') {
                    endIndex++;
                }
                return json.substring(startIndex, endIndex).trim();
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Main entry point for the BrokerServer application
     */
    public static void main(String[] args) {
        try {
            BrokerServer broker = new BrokerServer();
            broker.start();
            
            // Keep the server running
            System.out.println("\nBroker Server is running. Press Ctrl+C to stop.");
            
            // Add shutdown hook for graceful cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down Broker Server...");
                broker.stop();
            }));
            
            // Keep the main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("Failed to start Broker Server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}