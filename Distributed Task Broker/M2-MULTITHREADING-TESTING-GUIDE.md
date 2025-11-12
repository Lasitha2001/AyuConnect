# M2 Multi-threading Task Processing - Testing Guide

## 📋 Overview

**Module:** M2 - Multi-threading with ExecutorService  
**Components:** `TaskSubmissionHandler.java`, `TaskTcpReceiver.java` (enhanced), `WorkerClient.java` (enhanced)  
**Purpose:** Concurrently process tasks by splitting them into sub-tasks and dispatching to registered workers  
**Technologies:** Java ExecutorService, Callable, Future, TCP Sockets  
**Thread Pool:** 10 threads

---

## ✅ Implementation Status

- [x] `TaskSubmissionHandler.java` - Multi-threaded task processing engine
- [x] Sub-task splitting logic (1 task → 5 sub-tasks)
- [x] Callable implementation for concurrent dispatch
- [x] ExecutorService integration (10-thread pool)
- [x] TCP connection per sub-task for reliable delivery
- [x] WorkerClient TCP server for receiving sub-tasks
- [x] Sub-task processing and acknowledgment
- [x] TaskManager integration for result tracking

---

## 🎯 Success Criteria

The test is successful when you observe:

1. ✅ M1 receives task from OriginatorClient
2. ✅ **M2 creates 5 threads** using ExecutorService
3. ✅ **Task split into 5 sub-tasks**
4. ✅ **Each sub-task dispatched via NEW TCP connection**
5. ✅ **All 5 Worker Clients receive their sub-task**
6. ✅ Workers process sub-tasks concurrently (observable via terminals)
7. ✅ Workers send acknowledgments: `ACK:SUBTASK:X:COMPLETED`
8. ✅ Broker logs successful sub-task dispatch and acknowledgment

---

## 🚀 Quick Start - Testing Method

### Automated Test Script (Recommended)

```powershell
.\test-m2-multithreading.ps1
```

This script will:
1. Start Broker Server
2. Start 5 Worker Clients (ports 6000-6004)
3. Wait for workers to register
4. Submit a task from OriginatorClient
5. Observe M2 splitting task into 5 sub-tasks
6. Watch concurrent dispatch to all 5 workers

---

## 📊 Expected Output

### Terminal 1 - Broker Server:

```
M1: TCP Task Receiver listening on port 5000
M2: Executor Service initialized with 10 threads
[... other modules initialize ...]

Broker Server started successfully!

[After task submission:]
M1: Originator connected. Handing off to Executor...
M1: Received task data: Process large dataset with 1000000 records
TaskManager: New Task created with ID: 1001. Split into 5 sub-tasks.
M1: Task accepted with ID: 1001

========================================
M2: Starting multi-threaded task processing
M2: Task ID: 1001
M2: Task Data: Process large dataset with 1000000 records
========================================
M2: Splitting task into 5 sub-tasks...
M2:   - Sub-task 1 created
M2:   - Sub-task 2 created
M2:   - Sub-task 3 created
M2:   - Sub-task 4 created
M2:   - Sub-task 5 created
M2: Thread 1 created for sub-task dispatch to worker 127.0.0.1:6000
M2: Thread 2 created for sub-task dispatch to worker 127.0.0.1:6001
M2: Thread 3 created for sub-task dispatch to worker 127.0.0.1:6002
M2: Thread 4 created for sub-task dispatch to worker 127.0.0.1:6003
M2: Thread 5 created for sub-task dispatch to worker 127.0.0.1:6004
M2: All 5 sub-tasks submitted to ExecutorService
========================================

M2: [Thread pool-2-thread-1] Dispatching sub-task 1 to worker 127.0.0.1:6000
M2: [Thread pool-2-thread-2] Dispatching sub-task 2 to worker 127.0.0.1:6001
M2: [Thread pool-2-thread-3] Dispatching sub-task 3 to worker 127.0.0.1:6002
M2: [Thread pool-2-thread-4] Dispatching sub-task 4 to worker 127.0.0.1:6003
M2: [Thread pool-2-thread-5] Dispatching sub-task 5 to worker 127.0.0.1:6004
M2: [Thread pool-2-thread-1] Sub-task 1 sent to worker
M2: [Thread pool-2-thread-2] Sub-task 2 sent to worker
M2: [Thread pool-2-thread-3] Sub-task 3 sent to worker
M2: [Thread pool-2-thread-4] Sub-task 4 sent to worker
M2: [Thread pool-2-thread-5] Sub-task 5 sent to worker
M2: [Thread pool-2-thread-1] Sub-task 1 acknowledged by worker: ACK:SUBTASK:1:COMPLETED
TaskManager: Sub-task 1 completed.
M2: [Thread pool-2-thread-2] Sub-task 2 acknowledged by worker: ACK:SUBTASK:2:COMPLETED
TaskManager: Sub-task 2 completed.
M2: [Thread pool-2-thread-3] Sub-task 3 acknowledged by worker: ACK:SUBTASK:3:COMPLETED
TaskManager: Sub-task 3 completed.
M2: [Thread pool-2-thread-4] Sub-task 4 acknowledged by worker: ACK:SUBTASK:4:COMPLETED
TaskManager: Sub-task 4 completed.
M2: [Thread pool-2-thread-5] Sub-task 5 acknowledged by worker: ACK:SUBTASK:5:COMPLETED
TaskManager: Sub-task 5 completed.
```

### Terminals 2-6 - Worker Clients 1-5:

**Each worker shows similar output:**

```
Worker Client starting...
Broker Host: localhost
Worker Port: 6000
Worker: TCP Server listening on port 6000 for sub-tasks
Worker: Sent registration to broker
Worker: Registration acknowledged: REGISTERED
Worker: Connected to NIO broadcast channel

[When sub-task arrives:]
Worker: Broker connected to send sub-task

========================================
Worker: Received sub-task from broker
Worker: Sub-task data: TASK:1001:SUBTASK:1:SubTask-1/5: Process large dataset with 1000000 records [Partition 1]
========================================
Worker: Processing sub-task 1 for task 1001
Worker: Sub-task data: SubTask-1/5: Process large dataset with 1000000 records [Partition 1]
Worker: Sub-task 1 processing complete!
Worker: Sent acknowledgment: ACK:SUBTASK:1:COMPLETED
```

### Terminal 7 - Originator Client:

```
Originator Client starting...
Submitting task to broker: Process large dataset with 1000000 records
Task data sent to broker
Broker response: TASK_ACCEPTED:1001
Task submitted successfully with ID: 1001
```

---

## 🔧 Multi-threading Flow Diagram

```
OriginatorClient          Broker (M1 + M2)                5 Workers
     |                           |                            |
     |--- TCP: Task Data ------->|                            |
     |                           | [M1: Accept Task]          |
     |                           | [TaskManager.createTask()] |
     |<-- TASK_ACCEPTED:1001 ----|                            |
     |                           |                            |
     |                           | [M2: processTask()]        |
     |                           | [Split into 5 sub-tasks]   |
     |                           |                            |
     |                           | [ExecutorService.submit()] |
     |                           | [Create 5 Callable tasks]  |
     |                           |                            |
     |                    [Thread 1: SubTaskDispatcher]       |
     |                    [Thread 2: SubTaskDispatcher]       |
     |                    [Thread 3: SubTaskDispatcher]       |
     |                    [Thread 4: SubTaskDispatcher]       |
     |                    [Thread 5: SubTaskDispatcher]       |
     |                           |                            |
     |                           |--- TCP: SubTask 1 -------->| Worker 1
     |                           |--- TCP: SubTask 2 -------->| Worker 2
     |                           |--- TCP: SubTask 3 -------->| Worker 3
     |                           |--- TCP: SubTask 4 -------->| Worker 4
     |                           |--- TCP: SubTask 5 -------->| Worker 5
     |                           |                            |
     |                           |                     [Process SubTask 1]
     |                           |                     [Process SubTask 2]
     |                           |                     [Process SubTask 3]
     |                           |                     [Process SubTask 4]
     |                           |                     [Process SubTask 5]
     |                           |                            |
     |                           |<--- ACK:SUBTASK:1 ---------| Worker 1
     |                           |<--- ACK:SUBTASK:2 ---------| Worker 2
     |                           |<--- ACK:SUBTASK:3 ---------| Worker 3
     |                           |<--- ACK:SUBTASK:4 ---------| Worker 4
     |                           |<--- ACK:SUBTASK:5 ---------| Worker 5
     |                           |                            |
     |                           | [TaskManager.submitSubTaskResult()]
     |                           | [Track completion: 5/5]    |
     |                           |                            |
                            ✓ Task Complete!
```

---

## 📝 Code Implementation Details

### Key Files

1. **TaskSubmissionHandler.java** (NEW)
   - Location: `src/main/java/com/computenet/broker/service/TaskSubmissionHandler.java`
   - Methods:
     - `processTask()` - Main entry point for M2 processing
     - `splitTaskIntoSubTasks()` - Divides task into 5 sub-tasks
     - `SubTaskDispatcher` (inner class) - Callable for concurrent dispatch

2. **TaskTcpReceiver.java** (ENHANCED)
   - Added `TaskSubmissionHandler` integration
   - Calls `processTask()` after accepting task from originator

3. **WorkerClient.java** (ENHANCED)
   - Added `startTcpServer()` - Listens on worker port for sub-tasks
   - Added `handleSubTask()` - Processes received sub-tasks
   - Added `processSubTask()` - Simulates sub-task computation

4. **TaskManager.java**
   - `createTask()` - Creates task and determines sub-task count
   - `submitSubTaskResult()` - Records sub-task completion

### TaskSubmissionHandler - Key Code

```java
public void processTask(int taskId, String taskData) {
    // Get available workers
    List<TaskManager.WorkerDetails> workers = taskManager.getAvailableWorkers();
    
    // Split task into 5 sub-tasks
    List<String> subTasks = splitTaskIntoSubTasks(taskData, 5);
    
    // Create Callable for each sub-task
    for (int i = 0; i < 5; i++) {
        Callable<String> subTaskCallable = new SubTaskDispatcher(
            taskId, i+1, subTasks.get(i), workers.get(i), taskManager
        );
        
        // M2: Submit to ExecutorService for concurrent execution
        Future<String> future = executorService.submit(subTaskCallable);
    }
}
```

### SubTaskDispatcher - Callable Implementation

```java
@Override
public String call() throws Exception {
    // Establish NEW TCP connection to worker
    try (Socket workerSocket = new Socket(worker.address(), worker.tcpPort());
         PrintWriter out = new PrintWriter(workerSocket.getOutputStream(), true);
         BufferedReader in = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()))) {
        
        // Send sub-task data
        String message = "TASK:" + taskId + ":SUBTASK:" + subTaskId + ":" + subTaskData;
        out.println(message);
        
        // Wait for acknowledgment
        String response = in.readLine();
        
        // Record completion
        taskManager.submitSubTaskResult(taskId, subTaskId, response);
        
        return "SUCCESS: Sub-task " + subTaskId + " completed";
    }
}
```

### WorkerClient - TCP Server for Sub-tasks

```java
private void startTcpServer() {
    new Thread(() -> {
        tcpServer = new ServerSocket(workerTcpPort);
        
        while (running) {
            Socket clientSocket = tcpServer.accept();
            // Handle each sub-task in separate thread
            new Thread(() -> handleSubTask(clientSocket)).start();
        }
    }).start();
}

private void handleSubTask(Socket clientSocket) {
    try (BufferedReader in = new BufferedReader(...);
         PrintWriter out = new PrintWriter(...)) {
        
        String subTaskMessage = in.readLine();
        
        // Parse: TASK:taskId:SUBTASK:subTaskId:data
        // Process sub-task
        processSubTask(taskId, subTaskId, subTaskData);
        
        // Send acknowledgment
        out.println("ACK:SUBTASK:" + subTaskId + ":COMPLETED");
    }
}
```

---

## 🧪 Testing Scenarios

### Scenario 1: Basic M2 Test (5 Workers)

**Setup:** 1 Broker + 5 Workers + 1 Originator  
**Action:** Submit 1 task  
**Expected:** Task split into 5 sub-tasks, dispatched concurrently  

### Scenario 2: Multiple Tasks

**Setup:** 1 Broker + 5 Workers + Multiple Originators  
**Action:** Submit 3 tasks in sequence  
**Expected:** Each task split into 5 sub-tasks, processed independently  

### Scenario 3: Concurrent Task Submissions

**Setup:** 1 Broker + 5 Workers + 2 Originators  
**Action:** Submit tasks simultaneously from 2 originators  
**Expected:** ExecutorService handles both, total 10 sub-tasks dispatched  

---

## 🐛 Troubleshooting

### Problem: "No workers registered" in M2

**Solution:**
1. Ensure 5 workers started before submitting task
2. Check broker logs for "Worker registered in memory"
3. Wait 5 seconds after starting workers before task submission

### Problem: Worker doesn't receive sub-task

**Solution:**
1. Verify worker TCP server started: "TCP Server listening on port..."
2. Check firewall isn't blocking worker ports (6000-6004)
3. Ensure worker registered successfully with broker

### Problem: "Connection refused" when dispatching sub-task

**Solution:**
1. Worker TCP server must be running
2. Verify worker port matches registration port
3. Check worker hasn't crashed or exited

### Problem: Only some workers receive sub-tasks

**Solution:**
1. Ensure exactly 5 workers are registered
2. Check broker logs for number of registered workers
3. Verify all worker ports are unique (6000-6004)

---

## 📈 Performance Features

### ExecutorService Benefits:
- ✅ **10-thread pool** - Handles multiple tasks concurrently
- ✅ **Non-blocking** - Main thread continues accepting new tasks
- ✅ **Resource management** - Threads reused, not created per task
- ✅ **Scalable** - Can process multiple tasks with 5 sub-tasks each

### Concurrent Dispatch:
- ✅ **Parallel execution** - 5 sub-tasks sent simultaneously
- ✅ **Independent threads** - Each Callable runs in separate thread
- ✅ **Reliable delivery** - Each sub-task sent via dedicated TCP connection

### Worker Processing:
- ✅ **Concurrent handling** - Workers process sub-tasks independently
- ✅ **Simulated processing** - 2-second delay per sub-task
- ✅ **Acknowledgment protocol** - Confirms successful receipt and processing

---

## 🎓 Learning Outcomes

After completing this test, you will have verified:
- ✅ ExecutorService for concurrent task processing
- ✅ Callable and Future for asynchronous execution
- ✅ Task decomposition into sub-tasks
- ✅ Thread pool management (10 threads)
- ✅ Concurrent TCP connections (one per sub-task)
- ✅ Multi-threaded client-server communication
- ✅ Result aggregation and tracking

---

## ✅ Verification Checklist

- [ ] Broker started with M2 ExecutorService initialized
- [ ] 5 workers registered successfully
- [ ] Task submitted from originator
- [ ] "M2: Starting multi-threaded task processing" logged
- [ ] Task split into 5 sub-tasks
- [ ] 5 threads created for dispatch
- [ ] All 5 sub-tasks submitted to ExecutorService
- [ ] Broker logs show thread names: "Thread pool-2-thread-X"
- [ ] Each worker receives exactly one sub-task
- [ ] Workers display sub-task data in their terminals
- [ ] Workers process sub-tasks (2-second delay)
- [ ] Workers send acknowledgments
- [ ] Broker receives all 5 acknowledgments
- [ ] TaskManager records sub-task completions (5/5)

---

## 📚 Related Documentation

- **M1-TCP-TESTING-GUIDE.md** - TCP Task Submission testing
- **M4-HTTP-TESTING-GUIDE.md** - HTTP Data Loading testing
- **M5-UDP-TESTING-GUIDE.md** - UDP Worker Registration testing
- **README.md** - Full project overview

---

**Last Updated:** November 11, 2025  
**Version:** 1.0  
**Module:** M2 - Multi-threading with ExecutorService  
**Author:** Distributed Task Broker Project Team
