package com.computenet.broker.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * M4: Multicast Task Configuration Broadcaster
 * Broadcasts task configuration to all workers before sub-task dispatch
 */
public class TaskConfigMulticaster {
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int MULTICAST_PORT = 6005;
    private static final int TTL = 1; // Local network only
    
    private InetAddress multicastGroup;
    
    public TaskConfigMulticaster() {
        try {
            this.multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
            System.out.println("M4: Multicast initialized on " + MULTICAST_ADDRESS + ":" + MULTICAST_PORT);
        } catch (UnknownHostException e) {
            System.err.println("M4: Failed to initialize multicast group: " + e.getMessage());
        }
    }
    
    /**
     * Broadcasts task configuration to all workers via multicast
     * Enhanced format: "TASKCONFIG:taskId:taskName:splitCount:taskData:subTask1|subTask2|..."
     * 
     * @param taskId The unique task identifier
     * @param taskName The task name
     * @param splitCount Number of sub-tasks
     * @param taskData Original task data
     * @param subTasks Array of sub-task data strings
     * @return true if broadcast successful, false otherwise
     */
    public boolean broadcastTaskConfig(String taskId, String taskName, int splitCount, 
                                       String taskData, java.util.List<String> subTasks) {
        if (multicastGroup == null) {
            System.err.println("M4: Multicast group not initialized");
            return false;
        }
        
        // Join sub-tasks with pipe separator
        String subTasksJoined = String.join("|", subTasks);
        
        String message = String.format("TASKCONFIG:%s:%s:%d:%s:%s", 
            taskId, taskName, splitCount, taskData, subTasksJoined);
        
        try (MulticastSocket socket = new MulticastSocket()) {
            socket.setTimeToLive(TTL);
            
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                buffer, 
                buffer.length, 
                multicastGroup, 
                MULTICAST_PORT
            );
            
            socket.send(packet);
            System.out.println("M4: Broadcasted config for task " + taskId + 
                             " '" + taskName + "' (splits: " + splitCount + ")");
            return true;
            
        } catch (IOException e) {
            System.err.println("M4: Broadcast failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the multicast address for workers to join
     * @return Multicast address string
     */
    public static String getMulticastAddress() {
        return MULTICAST_ADDRESS;
    }
    
    /**
     * Gets the multicast port for workers to listen on
     * @return Multicast port number
     */
    public static int getMulticastPort() {
        return MULTICAST_PORT;
    }
}
