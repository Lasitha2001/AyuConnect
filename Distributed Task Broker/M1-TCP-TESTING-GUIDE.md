# M1 TCP Task Submission - Testing Guide

## 📋 Overview

**Module:** M1 - TCP Sockets for Reliable Task Submission  
**Components:** `TaskTcpReceiver.java`, `OriginatorClient.java`  
**Purpose:** Implement reliable task submission from originators to broker using blocking TCP sockets  
**Technologies:** Java ServerSocket, Socket, BufferedReader, PrintWriter  
**Port:** 5000 (TCP)

---

## ✅ Implementation Status

- [x] `TaskTcpReceiver.java` - Blocking TCP server on port 5000
- [x] `handleClient()` method - BufferedReader/PrintWriter for reliable communication
- [x] `OriginatorClient.java` - TCP client for task submission
- [x] Task acknowledgment protocol: `TASK_ACCEPTED:ID`
- [x] Integration with M2 ExecutorService for concurrent handling
- [x] Error handling and socket cleanup

---

## 🎯 Success Criteria

The test is successful when you observe:

1. ✅ Broker TCP listener started on port 5000
2. ✅ OriginatorClient sends string task data
3. ✅ Broker receives task data reliably via TCP
4. ✅ Broker acknowledges with `TASK_ACCEPTED:ID`
5. ✅ Task stored in TaskManager with unique ID
6. ✅ **M4 Integration**: Multicast config broadcast sent to enabled workers
7. ✅ Connection properly closed after submission

---

## 🤝 Integration with Other Modules

### M4 Multicast Integration
When a task is submitted via M1, the broker:
1. Receives task via TCP (M1)
2. **Broadcasts task config via M4 multicast** to all enabled workers
3. Waits 100ms for propagation
4. Splits task and dispatches sub-tasks (M2)

**Testing M1 with M4:**
- Start broker + workers
- **Enable M4 on some workers** via dashboard checkbox
- Submit task via M1 (OriginatorClient)
- **Enabled workers**: Show config in "Task Configs Received" tab
- **Disabled workers**: Receive nothing (no multicast subscription)

### M2 Multi-threading Integration
M1 hands off tasks to M2's ExecutorService for concurrent processing.

### M3 NIO Integration
Task submission triggers M3 NIO broadcasts to all workers.

### M5 UDP Integration
Workers must be registered via M5 to receive tasks from M1.

---

## Quick Start - Testing Methods

### Method 1: Automated Test Script (Recommended)

```powershell
.\test-m1-tcp-submission.ps1
```

This script will:
- Check if JAR exists (build if needed)
- Start the Broker Server
- Submit a test task via M1 TCP
- Display expected output guide

### Method 2: Manual JAR Execution

**Terminal 1 - Start Broker:**
```powershell
.\start-broker.ps1
```

**Terminal 2 - Submit Task:**
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Task: Process data"
```

---

## 📊 Expected Output

### On Broker Server Terminal:

```
M1: TCP Task Receiver listening on port 5000
M2: Executor Service initialized with 10 threads
M3: NIO Broadcast Handler started on port 5002
M4: HttpDataLoader initialized
M5: UDP Listener started on port 5001

[Javalin startup logs...]

Broker Server started successfully!
  TCP Task Receiver: port 5000
  UDP Worker Listener: port 5001
  NIO Broadcast Handler: port 5002
  Web UI: http://localhost:8080
  WebSocket: ws://localhost:8080/ws
  HTTP Data Loader API: http://localhost:8080/api/load-task

[When originator connects:]
M1: Originator connected. Handing off to Executor...
M1: Received task data: Task: Compute primes up to 1000
TaskManager: Task created with ID: 1
M1: Task accepted with ID: 1

M1: Originator connected. Handing off to Executor...
M1: Received task data: Task: Sort array [10000 elements]
TaskManager: Task created with ID: 2
M1: Task accepted with ID: 2

M1: Originator connected. Handing off to Executor...
M1: Received task data: Task: Process JSON data from API
TaskManager: Task created with ID: 3
M1: Task accepted with ID: 3
```

### On Originator Client Terminal:

```
Originator Client starting...
Submitting task to broker: Task: Compute primes up to 1000
Task data sent to broker
Broker response: TASK_ACCEPTED:1
Task submitted successfully with ID: 1
```

---

## 🔧 TCP Communication Flow

```
OriginatorClient              Broker (Port 5000)
     |                               |
     |------ TCP Connect ----------->|
     |                               | [Accept connection]
     |                               | [Create Socket]
     |                               |
     |------ Task Data String ------>| 
     |   "Task: Compute primes"      | [BufferedReader.readLine()]
     |                               |
     |                               | [TaskManager.createTask()]
     |                               | [Generate Task ID: 1]
     |                               |
     |<--- TASK_ACCEPTED:1 ----------| [PrintWriter.println()]
     |                               |
     | [Parse Task ID]              | [Close connection]
     | [Display success]             |
     |                               |
     |------ TCP Close ------------->|
     |                               |
     ✓ Task submitted!               ✓ Task stored!
```

---

## 📝 Code Implementation Details

### Key Files

1. **TaskTcpReceiver.java**
   - Location: `src/main/java/com/computenet/broker/server/TaskTcpReceiver.java`
   - Port: 5000
   - Mode: Blocking ServerSocket
   - Methods:
     - `run()` - Main server loop accepting connections
     - `handleClient(Socket)` - Processes individual client requests

2. **OriginatorClient.java**
   - Location: `src/main/java/com/computenet/client/OriginatorClient.java`
   - Methods:
     - `submitTask(String taskData)` - Sends task via TCP
     - `main(String[] args)` - Command-line interface
     - `interactiveMode()` - Submit multiple tasks

3. **TaskManager.java**
   - Location: `src/main/java/com/computenet/broker/service/TaskManager.java`
   - Method: `createTask(String taskData)` - Stores task and returns ID

### TaskTcpReceiver - handleClient() Implementation

```java
private void handleClient(Socket clientSocket) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
        
        // Read task data from client (BLOCKING)
        String taskData = in.readLine();
        System.out.println("M1: Received task data: " + taskData);
        
        // Create task in TaskManager
        int taskId = taskManager.createTask(taskData);
        
        // Send reliable acknowledgment back to client
        out.println("TASK_ACCEPTED:" + taskId);
        System.out.println("M1: Task accepted with ID: " + taskId);
        
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
```

### OriginatorClient - submitTask() Implementation

```java
public int submitTask(String taskData) {
    System.out.println("Submitting task to broker: " + taskData);
    
    try (Socket socket = new Socket(brokerHost, brokerPort);
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
        
        // Send task data
        out.println(taskData);
        System.out.println("Task data sent to broker");
        
        // Wait for acknowledgment (BLOCKING)
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
    }
    
    return -1;  // Failed
}
```

---

## 🧪 Advanced Testing Scenarios

### Test 1: Concurrent Task Submissions

Submit multiple tasks simultaneously to verify M2 ExecutorService handles concurrency:

```powershell
# Terminal 2
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Task 1"

# Terminal 3 (immediately)
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Task 2"

# Terminal 4 (immediately)
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Task 3"
```

**Expected:** All tasks processed concurrently, each gets unique ID.

### Test 2: Large Task Data

Test with longer task strings:

```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Task: Process dataset with 1000000 records, apply filters: age>25 AND city='NYC', aggregate by category, sort by revenue DESC"
```

**Expected:** Full string received and stored.

### Test 3: Special Characters

Test with special characters and JSON:

```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Task: {\"type\":\"compute\",\"operation\":\"sum\",\"data\":[1,2,3,4,5]}"
```

**Expected:** Special characters preserved in transmission.

### Test 4: Rapid Sequential Submissions

Submit tasks in quick succession:

```powershell
for ($i=1; $i -le 10; $i++) {
    java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Task $i: Process data"
}
```

**Expected:** All 10 tasks received with IDs 1-10.

---

## 🐛 Troubleshooting

### Problem: "Connection refused" on OriginatorClient

**Solution:**
1. Verify broker is running and TCP listener started
2. Check for "M1: TCP Task Receiver listening on port 5000" in broker logs
3. Ensure port 5000 is not blocked by firewall

```powershell
# Test if port 5000 is listening
netstat -ano | Select-String ":5000"
```

### Problem: Port 5000 already in use

**Solution:**
```powershell
# Find process using port 5000
netstat -ano | Select-String ":5000"

# Kill the process
taskkill /PID <process_id> /F
```

### Problem: Task not acknowledged

**Solution:**
1. Check broker logs for error messages
2. Verify TaskManager is initialized
3. Check if M2 ExecutorService is running
4. Ensure BufferedReader/PrintWriter are not null

### Problem: "Task submitted successfully with ID: -1"

**Solution:**
- Response parsing failed
- Check broker response format is `TASK_ACCEPTED:ID`
- Verify network connection didn't drop before acknowledgment

### Problem: Tasks not stored in TaskManager

**Solution:**
1. Check broker console for "TaskManager: Task created with ID: X"
2. Verify TaskManager.createTask() is being called
3. Check for exceptions in handleClient() method

---

## 📈 Performance & Reliability Features

### Blocking I/O Guarantees:
- ✅ **Reliable delivery** - TCP ensures packet delivery and ordering
- ✅ **Error detection** - TCP checksum validates data integrity
- ✅ **Flow control** - TCP prevents buffer overflow
- ✅ **Acknowledgment** - Application-level ACK confirms task accepted

### Concurrency (M2 Integration):
- ✅ **Thread pool** - 10 threads handle concurrent clients
- ✅ **Non-blocking accept** - Main thread continues accepting while tasks process
- ✅ **Isolated handling** - Each task processed independently

### Resource Management:
- ✅ **Auto-close** - Try-with-resources ensures socket cleanup
- ✅ **Error handling** - Exceptions caught and logged
- ✅ **Graceful shutdown** - Thread interruption support

---

## 🎓 Learning Outcomes

After completing this test, you will have verified:
- ✅ Blocking TCP ServerSocket implementation
- ✅ BufferedReader/PrintWriter for reliable text communication
- ✅ Request-response protocol design
- ✅ Multi-threaded client handling with ExecutorService
- ✅ Proper socket resource management (try-with-resources)
- ✅ Application-level acknowledgment protocol
- ✅ Error handling for network operations

---

## ✅ Verification Checklist

- [ ] Broker TCP listener started on port 5000
- [ ] "M1: TCP Task Receiver listening" message logged
- [ ] OriginatorClient connects successfully
- [ ] "M1: Originator connected" message logged
- [ ] Task data transmitted completely
- [ ] "M1: Received task data: ..." logged on broker
- [ ] Task stored in TaskManager
- [ ] "TaskManager: Task created with ID: X" logged
- [ ] Acknowledgment sent back to client
- [ ] "M1: Task accepted with ID: X" logged
- [ ] Client receives and parses acknowledgment
- [ ] "Task submitted successfully with ID: X" logged on client
- [ ] Socket closed properly (no resource leaks)
- [ ] Multiple concurrent submissions handled correctly

---

## 📚 Related Documentation

- **README.md** - Full project overview
- **M4-HTTP-TESTING-GUIDE.md** - HTTP Data Loading testing
- **M5-UDP-TESTING-GUIDE.md** - UDP Worker Registration testing
- **pom.xml** - Maven dependencies

---

## 🔗 Sample Task Data

### Simple Tasks:
```
"Task: Process data"
"Task: Compute prime numbers"
"Task: Sort array"
```

### JSON Tasks:
```
"Task: {\"type\":\"compute\",\"data\":[1,2,3,4,5]}"
"Task: {\"operation\":\"aggregate\",\"field\":\"revenue\"}"
```

### Complex Tasks:
```
"Task: Analyze sales data, filter by region='NA', aggregate by product, calculate total revenue"
"Task: Machine learning model training with 10000 samples, 50 features, 10 epochs"
```

---

**Last Updated:** November 11, 2025  
**Version:** 1.0  
**Module:** M1 - TCP Task Submission  
**Author:** Distributed Task Broker Project Team
