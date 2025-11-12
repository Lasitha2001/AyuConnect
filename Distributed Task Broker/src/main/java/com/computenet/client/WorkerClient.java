package com.computenet.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.javalin.Javalin;

/**
 * WorkerClient - The application run by the 5 members
 * Worker client that connects to the broker and processes tasks
 * M2: Multi-threaded Task Processing & Worker Interface
 *     - ExecutorService-based task splitting and distribution (Broker side)
 *     - TCP server to receive sub-tasks from broker (Worker side)
 *     - HTTP interface for manual task completion (Worker side)
 */
public class WorkerClient {
    
    private String brokerHost;
    private int brokerUdpPort = 5001;
    private int brokerNioPort = 5002;
    private int workerTcpPort = 6000; // Port where this worker listens for sub-tasks
    private boolean running = true;
    private ServerSocket tcpServer;
    private Javalin workerWebApp; // Worker's HTTP server
    
    // Store active sub-tasks for this worker
    private final Map<Integer, SubTaskInfo> activeSubTasks = new ConcurrentHashMap<>();
    private final AtomicInteger subTaskCounter = new AtomicInteger(0);
    
    // M4 Multicast: Manual join control
    private volatile boolean m4MulticastEnabled = false;
    private Thread multicastListenerThread = null;
    private java.net.MulticastSocket multicastSocket = null;
    
    // Store received M4 task configurations
    private final Map<String, TaskConfigInfo> receivedConfigs = new ConcurrentHashMap<>();
    
    // Record to hold sub-task information
    public record SubTaskInfo(int taskId, int subTaskId, String data, String status, long receivedTime) {}
    
    // Record to hold M4 task configuration information
    public record TaskConfigInfo(String taskId, String taskName, int splitCount, String taskData, 
                                  String[] subTasks, long receivedTime) {}
    
    public static void main(String[] args) {
        String brokerHost = args.length > 0 ? args[0] : "localhost";
        int workerPort = args.length > 1 ? Integer.parseInt(args[1]) : 6000;
        
        System.out.println("Worker Client starting...");
        System.out.println("Broker Host: " + brokerHost);
        System.out.println("Worker Port: " + workerPort);
        
        WorkerClient worker = new WorkerClient(brokerHost, workerPort);
        worker.start();
    }
    
    public WorkerClient(String brokerHost, int workerTcpPort) {
        this.brokerHost = brokerHost;
        this.workerTcpPort = workerTcpPort;
    }
    
    public void start() {
        // M2: Start HTTP server for worker web interface
        startWorkerWebInterface();
        
        // M2: Start TCP server to receive sub-tasks from broker
        startTcpServer();
        
        // Register with broker via UDP
        registerWithBroker();
        
        // M4: Multicast is now manually controlled via web UI (not auto-started)
        // Workers can enable/disable via checkbox in dashboard
        
        // Connect to NIO channel for receiving broadcasts
        connectToNIOChannel();
        
        // Start heartbeat thread
        startHeartbeat();
        
        // Keep worker running
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                running = false;
            }
        }
        
        // Cleanup
        try {
            if (tcpServer != null && !tcpServer.isClosed()) {
                tcpServer.close();
            }
            if (workerWebApp != null) {
                workerWebApp.stop();
            }
        } catch (IOException e) {
            System.err.println("Worker: Error closing TCP server: " + e.getMessage());
        }
    }
    
    /**
     * M2: Start HTTP server for worker web interface
     * Allows manual task completion via web UI
     */
    private void startWorkerWebInterface() {
        int httpPort = workerTcpPort + 1000; // HTTP on port 7000-7004
        
        workerWebApp = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(httpPort);
        
        System.out.println("Worker: Web interface started on http://localhost:" + httpPort);
        
        // Serve worker dashboard page with UTF-8 encoding for emoji support
        workerWebApp.get("/", ctx -> {
            String html = generateWorkerDashboardHTML();
            ctx.contentType("text/html; charset=utf-8");
            ctx.result(html);
        });
        
        // API: Get active sub-tasks
        workerWebApp.get("/api/tasks", ctx -> {
            ctx.json(activeSubTasks);
        });
        
        // API: Get worker info
        workerWebApp.get("/api/info", ctx -> {
            ctx.json(Map.of(
                "workerPort", workerTcpPort,
                "httpPort", httpPort,
                "brokerHost", brokerHost,
                "status", "RUNNING",
                "activeTaskCount", activeSubTasks.size(),
                "m4MulticastEnabled", m4MulticastEnabled
            ));
        });
        
        // API: Toggle M4 multicast subscription
        workerWebApp.post("/api/m4/toggle", ctx -> {
            try {
                // Parse JSON body: {"enabled": true/false}
                String body = ctx.body();
                boolean enable;
                
                if (body.contains("\"enabled\"")) {
                    // Extract enabled value from JSON
                    enable = body.contains("true");
                } else {
                    enable = Boolean.parseBoolean(body);
                }
                
                if (enable) {
                    // Enable multicast - start listener if not already running
                    if (!m4MulticastEnabled) {
                        m4MulticastEnabled = true;
                        startMulticastListener();
                        System.out.println("M4: Multicast listener ENABLED by user (localStorage)");
                    } else {
                        System.out.println("M4: Multicast already enabled");
                    }
                    ctx.json(Map.of("success", true, "message", "M4 multicast enabled", "enabled", true));
                } else {
                    // Disable multicast - stop listener if running
                    if (m4MulticastEnabled) {
                        stopMulticastListener();
                        m4MulticastEnabled = false;
                        System.out.println("M4: Multicast listener DISABLED by user (localStorage)");
                    } else {
                        System.out.println("M4: Multicast already disabled");
                    }
                    ctx.json(Map.of("success", true, "message", "M4 multicast disabled", "enabled", false));
                }
            } catch (Exception e) {
                System.err.println("M4: Error toggling multicast: " + e.getMessage());
                e.printStackTrace();
                ctx.json(Map.of("success", false, "message", "Error: " + e.getMessage()));
            }
        });
        
        // API: Get received M4 task configurations
        workerWebApp.get("/api/m4/configs", ctx -> {
            ctx.json(receivedConfigs);
        });
        
        // API: Mark sub-task as complete
        workerWebApp.post("/api/complete/{subTaskKey}", ctx -> {
            String subTaskKey = ctx.pathParam("subTaskKey");
            
            try {
                int key = Integer.parseInt(subTaskKey);
                SubTaskInfo task = activeSubTasks.get(key);
                
                if (task != null) {
                    // Update task status to completed
                    SubTaskInfo completedTask = new SubTaskInfo(
                        task.taskId(),
                        task.subTaskId(),
                        task.data(),
                        "COMPLETED",
                        task.receivedTime()
                    );
                    activeSubTasks.put(key, completedTask);
                    
                    System.out.println("Worker: Sub-task " + task.subTaskId() + " marked as COMPLETED");
                    
                    // Notify broker about completion to update statistics
                    notifyBrokerCompletion(task.taskId(), task.subTaskId());
                    
                    ctx.json(Map.of(
                        "success", true,
                        "message", "Sub-task marked as completed",
                        "subTaskId", task.subTaskId()
                    ));
                } else {
                    ctx.status(404).json(Map.of(
                        "success", false,
                        "message", "Sub-task not found"
                    ));
                }
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of(
                    "success", false,
                    "message", "Invalid sub-task key"
                ));
            }
        });
    }
    
    private void registerWithBroker() {
        try {
            DatagramSocket socket = new DatagramSocket();
            String message = "REGISTER:" + workerTcpPort;
            byte[] data = message.getBytes();
            
            InetAddress brokerAddress = InetAddress.getByName(brokerHost);
            DatagramPacket packet = new DatagramPacket(data, data.length, brokerAddress, brokerUdpPort);
            
            socket.send(packet);
            System.out.println("Worker: Sent registration to broker");
            
            // Wait for acknowledgment
            byte[] buffer = new byte[1024];
            DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(5000);
            socket.receive(ackPacket);
            
            String ack = new String(ackPacket.getData(), 0, ackPacket.getLength());
            System.out.println("Worker: Registration acknowledged: " + ack);
            
            socket.close();
        } catch (Exception e) {
            System.err.println("Worker: Registration failed: " + e.getMessage());
        }
    }
    
    /**
     * M4: Start multicast listener for task configuration broadcasts
     * Listens on multicast group 230.0.0.1:6005 for task metadata
     */
    private void startMulticastListener() {
        if (multicastListenerThread != null && multicastListenerThread.isAlive()) {
            System.out.println("M4: Multicast listener already running");
            return;
        }
        
        multicastListenerThread = new Thread(() -> {
            try {
                multicastSocket = new java.net.MulticastSocket(6005);
                multicastSocket.setSoTimeout(1000); // 1 second timeout for checking m4MulticastEnabled
                
                InetAddress group = InetAddress.getByName("230.0.0.1");
                java.net.NetworkInterface networkInterface = java.net.NetworkInterface.getByInetAddress(
                    InetAddress.getLocalHost()
                );
                multicastSocket.joinGroup(new InetSocketAddress(group, 6005), networkInterface);
                
                System.out.println("M4: Worker joined multicast group 230.0.0.1:6005");
                
                byte[] buffer = new byte[2048];
                
                while (m4MulticastEnabled && running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        multicastSocket.receive(packet); // Blocking call with timeout
                        
                        String message = new String(packet.getData(), 0, packet.getLength());
                        
                        // Parse enhanced format: "TASKCONFIG:taskId:taskName:splitCount:taskData:subTask1|subTask2|..."
                        if (message.startsWith("TASKCONFIG:")) {
                            parseAndStoreTaskConfig(message);
                        }
                    } catch (java.net.SocketTimeoutException e) {
                        // Timeout is normal - just check if we should continue
                        continue;
                    }
                }
                
                multicastSocket.leaveGroup(new InetSocketAddress(group, 6005), networkInterface);
                multicastSocket.close();
                multicastSocket = null;
                System.out.println("M4: Worker left multicast group");
            } catch (IOException e) {
                System.err.println("M4: Multicast listener error: " + e.getMessage());
            }
        });
        
        multicastListenerThread.start();
    }
    
    /**
     * M4: Stop multicast listener
     */
    private void stopMulticastListener() {
        m4MulticastEnabled = false;
        
        if (multicastSocket != null) {
            multicastSocket.close();
        }
        
        if (multicastListenerThread != null) {
            try {
                multicastListenerThread.join(2000); // Wait max 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * M4: Parse and store task configuration
     * Format: TASKCONFIG:taskId:taskName:splitCount:taskData:subTask1|subTask2|...
     */
    private void parseAndStoreTaskConfig(String message) {
        try {
            String[] parts = message.split(":", 6);
            if (parts.length >= 6) {
                String taskId = parts[1];
                String taskName = parts[2];
                int splitCount = Integer.parseInt(parts[3]);
                String taskData = parts[4];
                String[] subTasks = parts[5].split("\\|");
                
                long receivedTime = System.currentTimeMillis();
                
                TaskConfigInfo config = new TaskConfigInfo(
                    taskId, taskName, splitCount, taskData, subTasks, receivedTime
                );
                
                receivedConfigs.put(taskId, config);
                
                System.out.println("M4: Received and stored task config");
                System.out.println("M4:   Task ID: " + taskId);
                System.out.println("M4:   Task Name: " + taskName);
                System.out.println("M4:   Split Count: " + splitCount);
                System.out.println("M4:   Sub-tasks: " + subTasks.length);
            }
        } catch (Exception e) {
            System.err.println("M4: Error parsing task config: " + e.getMessage());
        }
    }
    
    private void connectToNIOChannel() {
        new Thread(() -> {
            while (running) {
                try {
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(brokerHost, brokerNioPort));
                    channel.configureBlocking(false);
                    
                    System.out.println("Worker: Connected to NIO broadcast channel");
                    
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    boolean connected = true;
                    
                    while (running && connected) {
                        buffer.clear();
                        int bytesRead = channel.read(buffer);
                        
                        if (bytesRead == -1) {
                            // Connection closed by broker
                            System.out.println("Worker: NIO connection closed by broker. Reconnecting...");
                            connected = false;
                            break;
                        } else if (bytesRead > 0) {
                            buffer.flip();
                            byte[] data = new byte[buffer.remaining()];
                            buffer.get(data);
                            String message = new String(data).trim();
                            
                            // M3: Just display the broadcast, don't process as a task
                            System.out.println("NIO: Received broadcast: " + message);
                        }
                        
                        Thread.sleep(100);
                    }
                    
                    channel.close();
                    
                    // Wait before reconnecting (if still running)
                    if (running && !connected) {
                        System.out.println("Worker: Waiting 2 seconds before reconnecting...");
                        Thread.sleep(2000);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Worker: NIO connection error: " + e.getMessage());
                    
                    // Wait before retrying if still running
                    if (running) {
                        try {
                            System.out.println("Worker: Waiting 5 seconds before retry...");
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            
            System.out.println("Worker: NIO connection thread stopped");
        }).start();
    }
    
    private void startHeartbeat() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                InetAddress brokerAddress = InetAddress.getByName(brokerHost);
                
                while (running) {
                    String message = "HEARTBEAT";
                    byte[] data = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, brokerAddress, brokerUdpPort);
                    socket.send(packet);
                    
                    Thread.sleep(10000); // Send heartbeat every 10 seconds
                }
                
                socket.close();
            } catch (Exception e) {
                System.err.println("Worker: Heartbeat error: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * M2: Notify broker when sub-task is completed
     * This allows broker to update worker statistics in real-time
     */
    private void notifyBrokerCompletion(int taskId, int subTaskId) {
        new Thread(() -> {
            try {
                // Use "localhost" to match how broker sees workers in local testing
                // In production, this should be the actual routable IP
                String workerAddress = "127.0.0.1";
                
                // Build JSON payload
                String json = String.format(
                    "{\"workerAddress\":\"%s\",\"workerPort\":\"%d\",\"taskId\":\"%d\",\"subTaskId\":\"%d\",\"result\":\"Completed\"}",
                    workerAddress, workerTcpPort, taskId, subTaskId
                );
                
                // Send HTTP POST to broker
                URL url = new URL("http://" + brokerHost + ":8080/api/worker-complete");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    System.out.println("Worker: Successfully notified broker of completion for sub-task " + 
                                       taskId + "-" + subTaskId);
                } else {
                    System.err.println("Worker: Failed to notify broker (HTTP " + responseCode + ")");
                }
                
                conn.disconnect();
            } catch (Exception e) {
                System.err.println("Worker: Error notifying broker of completion: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * M2: Starts TCP server to receive sub-tasks from broker
     * Each sub-task is sent via a new TCP connection from the broker's ExecutorService
     */
    private void startTcpServer() {
        new Thread(() -> {
            try {
                tcpServer = new ServerSocket(workerTcpPort);
                System.out.println("Worker: TCP Server listening on port " + workerTcpPort + " for sub-tasks");
                
                while (running) {
                    try {
                        Socket clientSocket = tcpServer.accept();
                        System.out.println("Worker: Broker connected to send sub-task");
                        
                        // Handle sub-task in separate thread to allow concurrent processing
                        new Thread(() -> handleSubTask(clientSocket)).start();
                        
                    } catch (SocketException e) {
                        if (!running) {
                            break; // Server is shutting down
                        }
                        System.err.println("Worker: Socket error: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Worker: TCP Server error: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * M2: Handles receiving and processing a sub-task from broker
     * Stores sub-task for manual completion via web UI
     */
    private void handleSubTask(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            // Receive sub-task from broker
            String subTaskMessage = in.readLine();
            
            if (subTaskMessage != null && subTaskMessage.startsWith("TASK:")) {
                System.out.println("\n========================================");
                System.out.println("Worker: Received sub-task from broker");
                System.out.println("Worker: Sub-task data: " + subTaskMessage);
                System.out.println("========================================");
                
                // Parse sub-task message: TASK:taskId:SUBTASK:subTaskId:data
                String[] parts = subTaskMessage.split(":", 5);
                if (parts.length >= 5) {
                    int taskId = Integer.parseInt(parts[1]);
                    int subTaskId = Integer.parseInt(parts[3]);
                    String subTaskData = parts[4];
                    
                    // Store sub-task for manual completion
                    int taskKey = subTaskCounter.incrementAndGet();
                    SubTaskInfo newTask = new SubTaskInfo(
                        taskId,
                        subTaskId,
                        subTaskData,
                        "PENDING",
                        System.currentTimeMillis()
                    );
                    activeSubTasks.put(taskKey, newTask);
                    
                    System.out.println("Worker: Sub-task stored with key " + taskKey);
                    System.out.println("Worker: Complete it via web interface at http://localhost:" + (workerTcpPort + 1000));
                    
                    // Send acknowledgment to broker
                    String ack = "ACK:SUBTASK:" + subTaskId + ":RECEIVED";
                    out.println(ack);
                    System.out.println("Worker: Sent acknowledgment: " + ack + "\n");
                } else {
                    System.err.println("Worker: Invalid sub-task message format");
                    out.println("ACK:ERROR:INVALID_FORMAT");
                }
            } else {
                System.err.println("Worker: Received non-task message: " + subTaskMessage);
            }
            
        } catch (IOException e) {
            System.err.println("Worker: Error handling sub-task: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Worker: Error closing client socket: " + e.getMessage());
            }
        }
    }
    
    /**
     * Generate HTML for worker dashboard
     */
    private String generateWorkerDashboardHTML() {
        int httpPort = workerTcpPort + 1000;
        
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Worker %d - Dashboard</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        
        .header {
            background: white;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            margin-bottom: 30px;
            text-align: center;
        }
        
        .header h1 {
            color: #667eea;
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .worker-info {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 20px;
        }
        
        .info-card {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 10px;
            border-left: 4px solid #667eea;
        }
        
        .info-card label {
            font-size: 0.9em;
            color: #666;
            display: block;
            margin-bottom: 5px;
        }
        
        .info-card span {
            font-size: 1.2em;
            font-weight: bold;
            color: #333;
        }
        
        .m4-control {
            background: #fff3cd;
            border: 2px solid #ffc107;
            border-radius: 10px;
            padding: 15px;
            margin-bottom: 30px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }
        
        .m4-control h3 {
            color: #856404;
            margin-bottom: 12px;
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 1.1em;
        }
        
        .checkbox-container {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 10px;
            background: white;
            border-radius: 6px;
        }
        
        .checkbox-container input[type="checkbox"] {
            width: 18px;
            height: 18px;
            cursor: pointer;
            flex-shrink: 0;
        }
        
        .checkbox-container label {
            font-size: 0.95em;
            cursor: pointer;
            user-select: none;
        }
        
        .m4-status {
            display: inline-block;
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 0.9em;
            font-weight: bold;
            margin-left: 10px;
        }
        
        .m4-status.enabled {
            background: #28a745;
            color: white;
        }
        
        .m4-status.disabled {
            background: #6c757d;
            color: white;
        }
        
        .tabs {
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
            overflow: hidden;
        }
        
        .tab-buttons {
            display: flex;
            background: #f8f9fa;
            border-bottom: 2px solid #e0e0e0;
        }
        
        .tab-btn {
            flex: 1;
            padding: 20px;
            background: none;
            border: none;
            font-size: 1.1em;
            font-weight: bold;
            color: #666;
            cursor: pointer;
            transition: all 0.3s;
            position: relative;
        }
        
        .tab-btn:hover {
            background: #e9ecef;
        }
        
        .tab-btn.active {
            color: #667eea;
            background: white;
        }
        
        .tab-btn.active::after {
            content: '';
            position: absolute;
            bottom: -2px;
            left: 0;
            right: 0;
            height: 3px;
            background: #667eea;
        }
        
        .tab-content {
            display: none;
            padding: 30px;
        }
        
        .tab-content.active {
            display: block;
        }
        
        .tasks-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 2px solid #e0e0e0;
        }
        
        .tasks-header h2 {
            color: #333;
        }
        
        .refresh-btn {
            background: #667eea;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 1em;
            transition: all 0.3s;
        }
        
        .refresh-btn:hover {
            background: #5568d3;
            transform: translateY(-2px);
        }
        
        .task-card {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 15px;
            border-left: 5px solid #667eea;
            transition: all 0.3s;
        }
        
        .task-card:hover {
            transform: translateX(5px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }
        
        .task-card.completed {
            border-left-color: #28a745;
            opacity: 0.7;
        }
        
        .config-card {
            background: #e7f3ff;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 15px;
            border-left: 5px solid #007bff;
            transition: all 0.3s;
        }
        
        .config-card:hover {
            transform: translateX(5px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }
        
        .task-header, .config-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }
        
        .task-id, .config-id {
            font-size: 1.2em;
            font-weight: bold;
            color: #667eea;
        }
        
        .status-badge {
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 0.9em;
            font-weight: bold;
        }
        
        .status-pending {
            background: #ffc107;
            color: #000;
        }
        
        .status-completed {
            background: #28a745;
            color: white;
        }
        
        .task-data, .config-data {
            background: white;
            padding: 15px;
            border-radius: 8px;
            margin: 10px 0;
            font-family: 'Courier New', monospace;
            color: #333;
            word-break: break-all;
        }
        
        .subtasks-list {
            background: white;
            padding: 15px;
            border-radius: 8px;
            margin: 10px 0;
        }
        
        .subtasks-list h4 {
            color: #667eea;
            margin-bottom: 10px;
        }
        
        .subtask-item {
            padding: 8px 12px;
            margin: 5px 0;
            background: #f8f9fa;
            border-radius: 5px;
            font-family: 'Courier New', monospace;
            border-left: 3px solid #667eea;
        }
        
        .task-meta, .config-meta {
            display: flex;
            gap: 20px;
            font-size: 0.9em;
            color: #666;
            margin: 10px 0;
            flex-wrap: wrap;
        }
        
        .complete-btn {
            background: #28a745;
            color: white;
            border: none;
            padding: 12px 25px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 1em;
            font-weight: bold;
            transition: all 0.3s;
            width: 100%%;
        }
        
        .complete-btn:hover:not(:disabled) {
            background: #218838;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(40, 167, 69, 0.3);
        }
        
        .complete-btn:disabled {
            background: #6c757d;
            cursor: not-allowed;
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #999;
        }
        
        .empty-state svg {
            width: 100px;
            height: 100px;
            margin-bottom: 20px;
            opacity: 0.3;
        }
        
        .back-link {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 20px;
            background: white;
            color: #667eea;
            text-decoration: none;
            border-radius: 8px;
            transition: all 0.3s;
        }
        
        .back-link:hover {
            background: #667eea;
            color: white;
            transform: translateY(-2px);
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>&#x1F5A5;&#xFE0F; Worker %d Dashboard</h1>
            <div class="worker-info" id="workerInfo">
                <div class="info-card">
                    <label>Worker TCP Port</label>
                    <span id="workerPort">%d</span>
                </div>
                <div class="info-card">
                    <label>HTTP Port</label>
                    <span id="httpPort">%d</span>
                </div>
                <div class="info-card">
                    <label>Status</label>
                    <span style="color: #28a745;">&#x25CF; RUNNING</span>
                </div>
                <div class="info-card">
                    <label>Active Tasks</label>
                    <span id="taskCount">0</span>
                </div>
            </div>
        </div>
        
        <!-- M4 Multicast Control -->
        <div class="m4-control">
            <h3>
                &#x1F4E1; Multicast Task Config Receiver
                <span class="m4-status disabled" id="m4Status">DISABLED</span>
            </h3>
            <div class="checkbox-container">
                <input type="checkbox" id="m4Checkbox" onchange="toggleM4Multicast()">
                <label for="m4Checkbox">
                    Enable to receive task configuration broadcasts from broker
                </label>
            </div>
        </div>
        
        <!-- Tabs -->
        <div class="tabs">
            <div class="tab-buttons">
                <button class="tab-btn active" onclick="switchTab('tasks')">
                    &#x1F4CB; Active Sub-Tasks
                </button>
                <button class="tab-btn" onclick="switchTab('m4configs')">
                    &#x1F4E1; Task Configs <span id="configBadge"></span>
                </button>
            </div>
            
            <!-- Active Tasks Tab -->
            <div id="tasksTab" class="tab-content active">
                <div class="tasks-header">
                    <h2>&#x1F4CB; Active Sub-Tasks</h2>
                    <button class="refresh-btn" onclick="loadTasks()">&#x1F504; Refresh</button>
                </div>
                <div id="tasksList"></div>
            </div>
            
            <!-- M4 Configs Tab -->
            <div id="m4configsTab" class="tab-content">
                <div class="tasks-header">
                    <h2>&#x1F4E1; Received Task Configurations</h2>
                    <button class="refresh-btn" onclick="loadM4Configs()">&#x1F504; Refresh</button>
                </div>
                <div id="configsList"></div>
            </div>
        </div>
        
        <div style="text-align: center;">
            <a href="http://localhost:8080/workers.html" class="back-link">&lt;- Back to Workers Registry</a>
        </div>
    </div>
    
    <script>
        let currentTab = 'tasks';
        const workerPort = %d; // Store worker port for localStorage key
        const localStorageKey = 'worker_' + workerPort + '_m4_enabled';
        
        function switchTab(tabName) {
            // Update tab buttons
            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            event.target.classList.add('active');
            
            // Update tab content
            document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
            document.getElementById(tabName + 'Tab').classList.add('active');
            
            currentTab = tabName;
            
            // Load data for the active tab
            if (tabName === 'tasks') {
                loadTasks();
            } else if (tabName === 'm4configs') {
                loadM4Configs();
            }
        }
        
        function loadWorkerInfo() {
            // Don't fetch from server - localStorage is the source of truth
            // Just verify the checkbox and badge are in sync
            const checkbox = document.getElementById('m4Checkbox');
            const savedState = localStorage.getItem(localStorageKey);
            
            if (savedState !== null) {
                const enabled = savedState === 'true';
                // Keep checkbox in sync with localStorage
                if (checkbox.checked !== enabled) {
                    checkbox.checked = enabled;
                }
                updateStatusBadge(enabled);
            }
        }
        
        function initializeM4State() {
            // Load saved state from localStorage - THIS IS THE SOURCE OF TRUTH
            const savedState = localStorage.getItem(localStorageKey);
            const checkbox = document.getElementById('m4Checkbox');
            
            if (savedState === 'true') {
                // User explicitly enabled it before
                checkbox.checked = true;
                updateStatusBadge(true);
                syncM4StateWithBackend(true);
            } else {
                // Default or explicitly disabled - keep UNCHECKED
                checkbox.checked = false;
                updateStatusBadge(false);
                // If there was no saved state, save false now
                if (savedState === null) {
                    localStorage.setItem(localStorageKey, 'false');
                }
                // Make sure backend is also disabled
                syncM4StateWithBackend(false);
            }
        }
        
        function syncM4StateWithBackend(enabled) {
            const status = document.getElementById('m4Status');
            
            fetch('/api/m4/toggle', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({enabled: enabled})
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    console.log('M4 state synced with backend:', enabled);
                } else {
                    console.error('Failed to sync M4 state:', data.message);
                }
            })
            .catch(error => {
                console.error('Error syncing M4 state:', error);
                updateStatusBadge(enabled);
            });
        }
        
        function updateStatusBadge(enabled) {
            const status = document.getElementById('m4Status');
            if (enabled) {
                status.textContent = 'ENABLED';
                status.className = 'm4-status enabled';
            } else {
                status.textContent = 'DISABLED';
                status.className = 'm4-status disabled';
            }
        }
        
        function toggleM4Multicast() {
            const checkbox = document.getElementById('m4Checkbox');
            const enabled = checkbox.checked;
            
            // Immediately save to localStorage
            localStorage.setItem(localStorageKey, enabled.toString());
            
            // Update status badge immediately for better UX
            updateStatusBadge(enabled);
            
            // Sync with backend
            fetch('/api/m4/toggle', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({enabled: enabled})
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    if (enabled) {
                        console.log('[OK] M4 Multicast enabled! This worker will now receive task configs.');
                    } else {
                        console.log('[OK] M4 Multicast disabled.');
                    }
                } else {
                    console.error('Error: ' + data.message);
                    // Revert checkbox and localStorage on error
                    checkbox.checked = !enabled;
                    localStorage.setItem(localStorageKey, (!enabled).toString());
                    updateStatusBadge(!enabled);
                    alert('Error: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error toggling M4 multicast:', error);
                // Revert checkbox and localStorage on error
                checkbox.checked = !enabled;
                localStorage.setItem(localStorageKey, (!enabled).toString());
                updateStatusBadge(!enabled);
                alert('Error toggling M4 multicast: ' + error);
            });
        }
        
        function loadTasks() {
            fetch('/api/tasks')
                .then(response => response.json())
                .then(tasks => {
                    const tasksList = document.getElementById('tasksList');
                    const taskCount = document.getElementById('taskCount');
                    
                    if (Object.keys(tasks).length === 0) {
                        tasksList.innerHTML = `
                            <div class="empty-state">
                                <svg viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm0 4c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm6 12H6v-1.4c0-2 4-3.1 6-3.1s6 1.1 6 3.1V19z"/>
                                </svg>
                                <h3>No Active Tasks</h3>
                                <p>This worker is waiting for sub-tasks from the broker.</p>
                            </div>
                        `;
                        taskCount.textContent = '0';
                    } else {
                        const tasksArray = Object.entries(tasks).map(([key, task]) => ({key, ...task}));
                        taskCount.textContent = tasksArray.length;
                        
                        tasksList.innerHTML = tasksArray.map(task => `
                            <div class="task-card ${task.status === 'COMPLETED' ? 'completed' : ''}">
                                <div class="task-header">
                                    <span class="task-id">Sub-Task #${task.subTaskId}</span>
                                    <span class="status-badge status-${task.status.toLowerCase()}">${task.status}</span>
                                </div>
                                <div class="task-meta">
                                    <span><strong>Task ID:</strong> ${task.taskId}</span>
                                    <span><strong>Received:</strong> ${new Date(task.receivedTime).toLocaleTimeString()}</span>
                                </div>
                                <div class="task-data">
                                    <strong>Data:</strong> ${task.data}
                                </div>
                                <button 
                                    class="complete-btn" 
                                    onclick="completeTask('${task.key}', ${task.subTaskId})"
                                    ${task.status === 'COMPLETED' ? 'disabled' : ''}>
                                    ${task.status === 'COMPLETED' ? '&#x2713; Completed' : '&#x2713; Mark as Complete'}
                                </button>
                            </div>
                        `).join('');
                    }
                })
                .catch(error => {
                    console.error('Error loading tasks:', error);
                });
        }
        
        function loadM4Configs() {
            fetch('/api/m4/configs')
                .then(response => response.json())
                .then(configs => {
                    const configsList = document.getElementById('configsList');
                    const configBadge = document.getElementById('configBadge');
                    
                    const configCount = Object.keys(configs).length;
                    configBadge.textContent = configCount > 0 ? `(${configCount})` : '';
                    
                    if (configCount === 0) {
                        configsList.innerHTML = `
                            <div class="empty-state">
                                <svg viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                                </svg>
                                <h3>No Task Configs Received</h3>
                                <p>Enable Multicast above and submit tasks from the broker to see configs here.</p>
                            </div>
                        `;
                    } else {
                        const configsArray = Object.entries(configs).map(([key, config]) => ({key, ...config}));
                        // Sort by received time descending (newest first)
                        configsArray.sort((a, b) => b.receivedTime - a.receivedTime);
                        
                        configsList.innerHTML = configsArray.map(config => `
                            <div class="config-card">
                                <div class="config-header">
                                    <span class="config-id">Task: ${config.taskName}</span>
                                    <span style="font-size: 0.9em; color: #666;">
                                        ${new Date(config.receivedTime).toLocaleString()}
                                    </span>
                                </div>
                                <div class="config-meta">
                                    <span><strong>Task ID:</strong> ${config.taskId}</span>
                                    <span><strong>Split Count:</strong> ${config.splitCount}</span>
                                    <span><strong>Sub-Tasks:</strong> ${config.subTasks.length}</span>
                                </div>
                                <div class="config-data">
                                    <strong>Original Task Data:</strong><br>
                                    ${config.taskData}
                                </div>
                                <div class="subtasks-list">
                                    <h4>&#x1F4E6; Sub-Tasks (${config.subTasks.length}):</h4>
                                    ${config.subTasks.map((subtask, idx) => `
                                        <div class="subtask-item">
                                            <strong>#${idx + 1}:</strong> ${subtask}
                                        </div>
                                    `).join('')}
                                </div>
                            </div>
                        `).join('');
                    }
                })
                .catch(error => {
                    console.error('Error loading M4 configs:', error);
                });
        }
        
        function completeTask(taskKey, subTaskId) {
            if (!confirm('Mark Sub-Task #' + subTaskId + ' as completed?')) {
                return;
            }
            
            fetch('/api/complete/' + taskKey, {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('[OK] Sub-Task #' + subTaskId + ' marked as completed!');
                    loadTasks();
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(error => {
                alert('Error completing task: ' + error);
            });
        }
        
        // Load data on page load
        initializeM4State(); // Initialize M4 state from localStorage first
        loadTasks();
        
        // Auto-refresh active tab every 5 seconds
        setInterval(() => {
            if (currentTab === 'tasks') {
                loadTasks();
            } else if (currentTab === 'm4configs') {
                loadM4Configs();
            }
            loadWorkerInfo(); // Refresh status badge only (not checkbox)
        }, 5000);
    </script>
</body>
</html>
        """.formatted(workerTcpPort, workerTcpPort, workerTcpPort, httpPort, workerTcpPort);
    }
    
    public void processTask(String taskData) {
        System.out.println("Worker: Processing task: " + taskData);
        
        // Simulate task processing
        try {
            Thread.sleep(1000);
            String result = "COMPLETED:" + taskData;
            System.out.println("Worker: Task completed: " + result);
        } catch (InterruptedException e) {
            System.err.println("Worker: Task processing interrupted");
        }
    }
    
    public void stop() {
        running = false;
    }
}
