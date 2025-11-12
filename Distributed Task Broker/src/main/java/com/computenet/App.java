package com.computenet;

import com.computenet.broker.server.BrokerServer;

/**
 * App - Main entry point
 * Starts the Broker Server
 */
public class App {
    
    public static void main(String[] args) {
        System.out.println("Starting Distributed Task Broker...");
        System.out.println("=====================================");
        
        try {
            // Initialize and start BrokerServer
            BrokerServer brokerServer = new BrokerServer();
            brokerServer.start();
            
            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down Broker Server...");
                brokerServer.stop();
            }));
            
        } catch (Exception e) {
            System.err.println("Failed to start Broker Server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
