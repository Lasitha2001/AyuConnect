# Complete Testing Guide - All Modules with Frontend

## ✅ Prerequisites Check

Your broker is already running! You should see:
```
✅ M1: TCP Task Receiver listening on port 5000
✅ M3: NIO Handler listening on port 5002
✅ M5: UDP Listener started on port 5001
✅ Web UI: http://localhost:8080
```

---

## 🌐 Step 1: Access the Web Dashboard

### Open the Frontend
1. Open your web browser
2. Navigate to: **http://localhost:8080**
3. You should see the **Distributed Task Broker Dashboard**

### What You'll See
- Real-time worker status
- Task submission interface
- WebSocket connection status
- System metrics

---

## 🧪 Step 2: Test M5 (UDP Worker Registration)

### Start Workers
Open **5 new PowerShell terminals** and run these commands:

**Terminal 1 - Worker 1:**
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6000
```

**Terminal 2 - Worker 2:**
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6001
```

**Terminal 3 - Worker 3:**
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6002
```

**Terminal 4 - Worker 4:**
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6003
```

**Terminal 5 - Worker 5:**
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6004
```

### ✅ Expected Output (Each Worker)
```
Worker Client starting...
Broker Host: localhost
Worker Port: 6000, 6001, 6002, 6003, 6004 (respectively)
Worker: Sent registration to broker
Worker: Registration acknowledged: REGISTERED
Worker: Connected to NIO broadcast channel
M5: Sending heartbeat to broker
```

### ✅ Expected Output (Broker Terminal)
```
M5: Received registration from /127.0.0.1:xxxxx
M5: Worker registered: [Address=/127.0.0.1, TcpPort=6000]
M3: Worker connected from /127.0.0.1:xxxxx
(Repeats for all 5 workers)
```

### ✅ Check Frontend
- **Refresh the browser** (http://localhost:8080)
- You should see **5 active workers** in the dashboard
- Worker status should show their addresses and ports

---

## 🧪 Step 3: Test M1 (TCP Task Submission)

### Option A: Using OriginatorClient (Command Line)
Open a **new PowerShell terminal**:

```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Matrix multiplication dataset"
```

### ✅ Expected Output (Originator)
```
Connected to broker at localhost:5000
Originator sent task: [TaskId=1001, Data=Matrix multiplication dataset]
Broker response: ACK:1001
Task submission successful!
```

### ✅ Expected Output (Broker Terminal)
```
M1: TCP Task Receiver listening on port 5000
M1: Originator connected. Handing off to Executor...
M1: Received task: [TaskId=1001, Data=Matrix multiplication dataset]
M1: Sending acknowledgment: ACK:1001
```

### Option B: Using Frontend (index.html)
1. Open browser: http://localhost:8080/index.html
2. Fill in the form:
   - **Task ID**: `1001` (number)
   - **Task Name**: `Matrix Multiplication Dataset`
   - **Number of Sub-Tasks**: Select `5 sub-tasks` from dropdown (if 5 workers registered)
   - **Sub-task 1 Data**: `Matrix batch 1`
   - **Sub-task 2 Data**: `Matrix batch 2`
   - **Sub-task 3 Data**: `Matrix batch 3`
   - **Sub-task 4 Data**: `Matrix batch 4`
   - **Sub-task 5 Data**: `Matrix batch 5`
   (Note: Sub-task input fields appear dynamically when you select sub-task count)
3. Click **"Submit Task via M1 TCP"** button
4. Watch the real-time status update and success message

---

## 🧪 Step 4: Test M2 (Multi-threading with ExecutorService)

### What Happens After Task Submission
When you submit a task via M1, M2 automatically:
1. Splits the task into **5 sub-tasks**
2. Creates **5 concurrent threads** using ExecutorService
3. Dispatches each sub-task to a different worker

### ✅ Expected Output (Broker Terminal)
```
========================================
M2: Starting multi-threaded task processing
M2: Task ID: 1001
M2: Task Data: Matrix multiplication dataset
========================================
M3: NIO handler notified of new task 1001
M2: Thread 1 created for sub-task dispatch to worker localhost:6000
M2: Thread 2 created for sub-task dispatch to worker localhost:6001
M2: Thread 3 created for sub-task dispatch to worker localhost:6002
M2: Thread 4 created for sub-task dispatch to worker localhost:6003
M2: Thread 5 created for sub-task dispatch to worker localhost:6004
M2: All 5 sub-tasks submitted to ExecutorService
M2: [Thread pool-1-thread-1] Dispatching sub-task 1 to worker localhost:6000
M2: [Thread pool-1-thread-2] Dispatching sub-task 2 to worker localhost:6001
M2: [Thread pool-1-thread-3] Dispatching sub-task 3 to worker localhost:6002
M2: [Thread pool-1-thread-4] Dispatching sub-task 4 to worker localhost:6003
M2: [Thread pool-1-thread-5] Dispatching sub-task 5 to worker localhost:6004
M2: Sub-task 1 completed successfully
M2: Sub-task 2 completed successfully
M2: Sub-task 3 completed successfully
M2: Sub-task 4 completed successfully
M2: Sub-task 5 completed successfully
```

### ✅ Expected Output (Worker Terminals - Each One)
```
M2: Received sub-task from broker: TASK:1001:SUBTASK:1:Matrix multiplication dataset - Part 1/5
M2: Processing sub-task 1 for task 1001
M2: Sub-task 1 completed. Sending ACK to broker
```

### ✅ Check Frontend
- Task status should update to "Processing"
- You may see worker activity indicators
- Check the WebSocket console for real-time updates

---

## 🧪 Step 5: Test M3 (Java NIO Broadcast)

### What to Observe
M3 broadcasts task progress **every 2 seconds** to all connected workers via NIO.

### ✅ Expected Output (Broker Terminal - Every 2 Seconds)
```
M3: Selector detected 5 ready keys
M3: Active NIO channels: 5
M3: Broadcasting task progress update
M3: Broadcast sent to worker /127.0.0.1:xxxxx (28 bytes)
M3: Broadcast sent to worker /127.0.0.1:xxxxx (28 bytes)
M3: Broadcast sent to worker /127.0.0.1:xxxxx (28 bytes)
M3: Broadcast sent to worker /127.0.0.1:xxxxx (28 bytes)
M3: Broadcast sent to worker /127.0.0.1:xxxxx (28 bytes)
```

### ✅ Expected Output (Worker Terminals - Every 2 Seconds)
```
NIO: Received broadcast: PROGRESS:1001:Processing...
NIO: Received broadcast: PROGRESS:1001:Processing...
NIO: Received broadcast: PROGRESS:1001:Processing...
```

### Timing Test
Watch the broker and worker terminals for **15 seconds**. You should see:
- **~7 broadcasts** (15 seconds ÷ 2-second interval)
- All 5 workers receiving each broadcast
- No missed broadcasts or errors

---

## 🧪 Step 6: Test M4 (HTTP Data Loading)

### Method 1: Using REST API Endpoint

Open a **new PowerShell terminal**:

```powershell
# Test with default URL
Invoke-RestMethod -Uri "http://localhost:8080/api/load-task?url=https://jsonplaceholder.typicode.com/todos/1" -Method Get
```

### ✅ Expected Output
```json
{
  "userId": 1,
  "id": 1,
  "title": "delectus aut autem",
  "completed": false
}
```

### Method 2: Using PowerShell Script

```powershell
.\test-m4-multicast.ps1
```

### ✅ Expected Output (Broker Terminal)
```
M4: HTTP GET https://jsonplaceholder.typicode.com/todos/1 - Response Code: 200
M4: Successfully loaded 84 bytes
```

### Method 3: Using Frontend (If Form Available)
1. Look for **"Load Task Data"** section in dashboard
2. Enter URL: `https://jsonplaceholder.typicode.com/todos/1`
3. Click **"Load Data"** button
4. Data should appear in the response area

---

## 🧪 Step 7: Full Integration Test (All Modules Together)

### Run the Complete Flow

1. **Ensure 5 workers are running** (Step 2)
2. **Submit a task via M1** (Step 3)
3. **Observe M2 multi-threading** (Step 4) - Task splits into 5 sub-tasks
4. **Monitor M3 NIO broadcasts** (Step 5) - Updates every 2 seconds
5. **Workers receive and process** - Check worker terminals
6. **Load HTTP data with M4** (Step 6) - Test external data loading
7. **M5 heartbeats continue** - Workers send heartbeats every 5 seconds

### ✅ Complete Success Indicators

**Broker Terminal:**
```
M1: Originator connected ✅
M2: All 5 sub-tasks submitted to ExecutorService ✅
M3: Broadcasting task progress update (every 2s) ✅
M4: Successfully loaded X bytes ✅
M5: Worker registered (×5) ✅
```

**Worker Terminals (All 5):**
```
Worker: Registration acknowledged ✅
Worker: Connected to NIO broadcast channel ✅
M2: Received sub-task from broker ✅
NIO: Received broadcast: PROGRESS:1001:Processing... ✅
M5: Sending heartbeat to broker ✅
```

**Frontend Dashboard:**
```
✅ 5 active workers displayed
✅ Task status shows "Processing" or "Completed"
✅ WebSocket connected
✅ Real-time updates working
```

---

## 📊 Frontend Dashboard Features

### What to Check

1. **Worker Status Panel**
   - Number of active workers: Should show **5**
   - Worker addresses and ports
   - Last heartbeat timestamps

2. **Task Submission Panel**
   - Input fields for Task ID and Data
   - Submit button
   - Response/acknowledgment display

3. **WebSocket Status**
   - Connection indicator (green = connected)
   - Real-time message log
   - Status updates

4. **HTTP Data Loader**
   - URL input field
   - Load button
   - Response display area

5. **System Metrics**
   - Active connections
   - Tasks processed
   - Uptime

---

## 🔧 Troubleshooting

### Workers Not Showing in Dashboard
```powershell
# Check if workers are registered
# Look for "M5: Worker registered" in broker terminal

# Refresh browser
# Press F5 or Ctrl+R

# Check WebSocket connection
# Open browser console (F12) and look for WebSocket messages
```

### No M3 Broadcasts
```powershell
# Submit a task first to trigger broadcasts
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost 2001 "Test task"

# Wait 2 seconds and check broker terminal for "M3: Broadcasting"
```

### M4 HTTP Loading Fails
```powershell
# Test with a different URL
Invoke-RestMethod -Uri "http://localhost:8080/api/load-task?url=https://httpbin.org/json" -Method Get
```

### Port Conflicts
```powershell
# Check what's using the ports
netstat -ano | findstr ":5000"
netstat -ano | findstr ":5001"
netstat -ano | findstr ":5002"
netstat -ano | findstr ":8080"

# Kill conflicting processes
taskkill /PID <PID> /F
```

---

## 🎯 Quick Test Checklist

Run through this checklist to verify everything:

- [ ] Broker started successfully
- [ ] Frontend accessible at http://localhost:8080
- [ ] 5 workers started and registered (M5)
- [ ] Workers shown in dashboard
- [ ] Task submitted via M1 TCP
- [ ] Task split into 5 sub-tasks (M2)
- [ ] All 5 workers received sub-tasks
- [ ] NIO broadcasts every 2 seconds (M3)
- [ ] Workers receiving broadcasts
- [ ] HTTP data loading works (M4)
- [ ] Heartbeats every 5 seconds (M5)
- [ ] WebSocket updates in real-time
- [ ] No errors in any terminal

---

## 📸 Expected Terminal Layout

### Recommended Setup

```
┌─────────────────────┬─────────────────────┬─────────────────────┐
│   Broker Server     │   Worker 1         │   Worker 2         │
│   (Port 8080)       │   (Port 6000)      │   (Port 6001)      │
│                     │                    │                    │
│ M1: TCP listening   │ Registration OK    │ Registration OK    │
│ M3: NIO broadcasts  │ NIO connected      │ NIO connected      │
│ M5: UDP listening   │ Sub-task received  │ Sub-task received  │
└─────────────────────┴─────────────────────┴─────────────────────┘

┌─────────────────────┬─────────────────────┬─────────────────────┐
│   Worker 3         │   Worker 4         │   Worker 5         │
│   (Port 6002)      │   (Port 6003)      │   (Port 6004)      │
│                    │                    │                    │
│ Registration OK    │ Registration OK    │ Registration OK    │
│ NIO connected      │ NIO connected      │ NIO connected      │
│ Sub-task received  │ Sub-task received  │ Sub-task received  │
└─────────────────────┴─────────────────────┴─────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│   Web Browser - http://localhost:8080                          │
│                                                                 │
│   Dashboard showing: 5 active workers, tasks, real-time data   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Next Steps After Testing

1. **Submit Multiple Tasks**
   - Try different task IDs (1001, 1002, 1003)
   - Test with different data strings
   - Observe concurrent processing

2. **Test Worker Resilience**
   - Stop one worker (Ctrl+C)
   - Submit a task with only 4 workers
   - Restart the worker
   - Verify it re-registers

3. **Monitor Performance**
   - Submit 10 tasks rapidly
   - Watch ExecutorService handle the load
   - Check NIO broadcast performance

4. **Web Dashboard Interaction**
   - Use all frontend features
   - Test WebSocket real-time updates
   - Monitor worker status changes

---

## ✅ Success Criteria

Your system is fully operational when:

1. ✅ All 5 modules working (M1-M5)
2. ✅ Frontend shows real-time data
3. ✅ Workers process sub-tasks successfully
4. ✅ NIO broadcasts reach all workers
5. ✅ HTTP data loading functional
6. ✅ WebSocket connection stable
7. ✅ No errors in any terminal
8. ✅ Tasks complete successfully

---

**Ready to start? Follow the steps above and watch your distributed system in action!** 🎉
