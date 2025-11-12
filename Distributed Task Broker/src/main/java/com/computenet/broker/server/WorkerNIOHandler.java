package com.computenet.broker.server;

import com.computenet.broker.service.TaskManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * WorkerNIOHandler - Member 3
 * Handles NIO-based communication with workers for non-blocking broadcasts
 * M3: Uses Selector for non-blocking I/O and broadcasts task progress every 2 seconds
 */
public class WorkerNIOHandler implements Runnable {
    
    private final Selector selector;
    private final TaskManager taskManager;
    private final int nioPort = 5002;
    private ServerSocketChannel serverChannel;
    private long lastBroadcastTime = 0;
    private static final long BROADCAST_INTERVAL = 2000; // 2 seconds
    private int currentTaskId = 0; // Track current task for progress updates
    
    public WorkerNIOHandler(Selector selector, TaskManager taskManager) {
        this.selector = selector;
        this.taskManager = taskManager;
    }
    
    @Override
    public void run() {
        try {
            // Set up NIO server channel
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(nioPort));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            System.out.println("M3: NIO Handler listening on port " + nioPort);
            System.out.println("M3: Non-blocking broadcast enabled (every 2 seconds)");
            
            while (!Thread.currentThread().isInterrupted()) {
                // M3: Non-blocking select with timeout
                int readyKeys = selector.select(100); // Short timeout for broadcast timing
                
                // M3: Check if it's time to broadcast
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBroadcastTime >= BROADCAST_INTERVAL) {
                    broadcastTaskProgress();
                    lastBroadcastTime = currentTime;
                }
                
                if (readyKeys == 0) {
                    continue;
                }
                
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                
                System.out.println("M3: Selector detected " + readyKeys + " ready keys");
                
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    
                    if (!key.isValid()) {
                        continue;
                    }
                    
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("M3: NIO Handler error: " + e.getMessage());
        }
    }
    
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            
            String workerKey = clientChannel.getRemoteAddress().toString();
            taskManager.registerNIOChannel(clientChannel, workerKey);
            System.out.println("M3: Worker connected via NIO: " + workerKey);
        }
    }
    
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            channel.close();
            key.cancel();
            return;
        }
        
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        String message = new String(data).trim();
        
        System.out.println("M3: Received from worker: " + message);
        
        // Process worker response (e.g., task results)
        if (message.startsWith("RESULT:")) {
            String[] parts = message.split(":");
            if (parts.length >= 3) {
                int taskId = Integer.parseInt(parts[1]);
                int subTaskId = Integer.parseInt(parts[2]);
                String result = parts.length > 3 ? parts[3] : "";
                taskManager.submitSubTaskResult(taskId, subTaskId, result);
            }
        }
    }
    
    /**
     * M3: Broadcasts task progress to all connected workers via NIO
     * Uses non-blocking channels and Selector to send updates without blocking
     */
    private void broadcastTaskProgress() {
        // Get all registered NIO channels from selector
        Set<SelectionKey> allKeys = selector.keys();
        int activeChannels = 0;
        int successfulBroadcasts = 0;
        
        // Count active worker channels (exclude server channel)
        for (SelectionKey key : allKeys) {
            if (key.channel() instanceof SocketChannel && key.isValid()) {
                activeChannels++;
            }
        }
        
        if (activeChannels == 0) {
            // No workers connected, skip broadcast
            return;
        }
        
        // Get current task progress from TaskManager
        String progressMessage = taskManager.getTaskProgress(currentTaskId);
        String broadcastData = "PROGRESS:" + currentTaskId + ":" + progressMessage + "\n";
        ByteBuffer buffer = ByteBuffer.wrap(broadcastData.getBytes());
        
        System.out.println("\n========================================");
        System.out.println("M3: Broadcasting task progress update");
        System.out.println("M3: Active NIO channels: " + activeChannels);
        System.out.println("M3: Message: " + broadcastData.trim());
        System.out.println("========================================");
        
        // Broadcast to all worker channels
        for (SelectionKey key : allKeys) {
            if (key.channel() instanceof SocketChannel && key.isValid()) {
                SocketChannel channel = (SocketChannel) key.channel();
                try {
                    buffer.rewind(); // Reset buffer position for each channel
                    int bytesWritten = channel.write(buffer);
                    
                    if (bytesWritten > 0) {
                        System.out.println("M3: Broadcast sent to " + channel.getRemoteAddress() + 
                                         " (" + bytesWritten + " bytes)");
                        successfulBroadcasts++;
                    }
                } catch (IOException e) {
                    System.err.println("M3: Error broadcasting to channel: " + e.getMessage());
                    // Channel may be closed, cancel the key
                    key.cancel();
                    try {
                        channel.close();
                    } catch (IOException closeEx) {
                        // Ignore close exception
                    }
                }
            }
        }
        
        System.out.println("M3: Broadcast complete - " + successfulBroadcasts + "/" + activeChannels + " successful\n");
    }
    
    private void handleWrite(SelectionKey key) throws IOException {
        // Broadcast task progress to workers
        SocketChannel channel = (SocketChannel) key.channel();
        // Implementation for broadcasting updates
    }
    
    /**
     * M3: Sets the current task ID for progress tracking
     * Called by M2 when a new task starts processing
     */
    public void setCurrentTask(int taskId) {
        this.currentTaskId = taskId;
        System.out.println("M3: Now tracking progress for task " + taskId);
    }
    
    public void stop() {
        try {
            if (serverChannel != null) {
                serverChannel.close();
            }
        } catch (IOException e) {
            System.err.println("M3: Error stopping NIO handler: " + e.getMessage());
        }
    }
}
