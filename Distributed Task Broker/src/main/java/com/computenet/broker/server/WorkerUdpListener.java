package com.computenet.broker.server;

import com.computenet.broker.service.TaskManager;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * WorkerUdpListener - Member 5
 * Listens for UDP messages from workers for registration and heartbeats
 */
public class WorkerUdpListener implements Runnable {
    
    private final TaskManager taskManager;
    private final int udpPort = 5001;
    private DatagramSocket datagramSocket;
    
    public WorkerUdpListener(TaskManager taskManager) {
        this.taskManager = taskManager;
    }
    
    @Override
    public void run() {
        try {
            datagramSocket = new DatagramSocket(udpPort);
            byte[] buffer = new byte[1024];
            
            System.out.println("M5: UDP Listener started on port " + udpPort);
            
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet); // Blocking call
                
                String message = new String(packet.getData(), 0, packet.getLength()).trim();
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                
                System.out.println("M5: Received UDP message: " + message + " from " + clientAddress + ":" + clientPort);
                
                // Parse worker registration message
                // Expected format: "REGISTER:TCP_PORT"
                if (message.startsWith("REGISTER:")) {
                    String[] parts = message.split(":");
                    if (parts.length >= 2) {
                        int tcpPort = Integer.parseInt(parts[1]);
                        taskManager.registerWorker(clientAddress.getHostAddress(), tcpPort);
                        
                        // Send acknowledgment back to worker
                        String ack = "REGISTERED";
                        byte[] ackData = ack.getBytes();
                        DatagramPacket ackPacket = new DatagramPacket(
                            ackData, ackData.length, clientAddress, clientPort
                        );
                        datagramSocket.send(ackPacket);
                    }
                } else if (message.equals("HEARTBEAT")) {
                    // Handle heartbeat from worker
                    System.out.println("M5: Heartbeat received from " + clientAddress);
                    
                    // Send heartbeat acknowledgment
                    String ack = "ACK";
                    byte[] ackData = ack.getBytes();
                    DatagramPacket ackPacket = new DatagramPacket(
                        ackData, ackData.length, clientAddress, clientPort
                    );
                    datagramSocket.send(ackPacket);
                }
            }
        } catch (Exception e) {
            if (!Thread.currentThread().isInterrupted()) {
                System.err.println("M5: UDP Listener error: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }
    
    public void stop() {
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
            System.out.println("M5: UDP Listener stopped");
        }
    }
}
