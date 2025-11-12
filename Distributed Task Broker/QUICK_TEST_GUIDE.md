# Quick Testing Guide - Distributed Task Broker

## Fastest Way to Test Everything (5 Minutes)

### Step 1: Build (30 seconds)
```powershell
mvn clean package -DskipTests
```

### Step 2: Start Broker
```powershell
.\start-broker.ps1
```

**Wait for this line:**
```
Web UI: http://localhost:8080
```

### Step 3: Start 5 Workers
```powershell
.\start-workers.ps1
```

**Expected:** 5 PowerShell windows open, each showing:
```
Worker X (TCP:600X, HTTP:700X)
Worker-X started. Listening on port 600X
Web interface: http://localhost:700X
```

### Step 4: Test M4 Multicast (Manual Control)

#### Enable Multicast on Workers
1. Open worker dashboards in browser:
   - Worker 1: http://localhost:7000
   - Worker 2: http://localhost:7001
   - Worker 3: http://localhost:7002
   - Worker 4: http://localhost:7003
   - Worker 5: http://localhost:7004

2. **Enable M4 on Workers 1, 2, 3** (checkbox at top):
   - Check the box: Enable Multicast Task Config Receiver
   - Status should show: ENABLED (green circle)

3. **Keep Workers 4, 5 DISABLED**:
   - Leave checkbox unchecked
   - Status should show: DISABLED (red circle)

### Step 5: Test Web Interface

#### Task Submission (index.html)
1. Open browser: http://localhost:8080/index.html
2. Fill in the form:
   - **Task ID**: `1001` (number)
   - **Task Name**: `Customer Analytics Processing`
   - **Number of Sub-Tasks**: Select from dropdown (e.g., `5 sub-tasks` if 5 workers registered)
   - **Sub-task 1 Data**: `Process batch 1`
   - **Sub-task 2 Data**: `Process batch 2`
   - **Sub-task 3 Data**: `Process batch 3`
   - **Sub-task 4 Data**: `Process batch 4`
   - **Sub-task 5 Data**: `Process batch 5`
   (Note: Sub-task input fields appear dynamically based on selected count)
3. Click **"Submit Task via M1 TCP"**
4. Verify green success message appears with task ID and sub-task count

#### Real-Time Monitoring (dashboard.html)
1. Open new tab: http://localhost:8080/dashboard.html
2. Verify:
   - "WebSocket: Connected" (green)
   - "Active Workers: 5"
   - M3 NIO broadcasts appearing every 2 seconds
   - All 5 module health indicators green

#### Verify M4 Multicast Reception
1. Go back to **Worker 1, 2, 3 dashboards**
2. Click **"Task Configs Received"** tab
3. Verify config appears:
   ```
   Task ID: 1001
   Task Name: Customer Analytics Processing
   Split Count: 5
   Received: [timestamp]
   Sub-tasks: Task-1001-1, Task-1001-2, Task-1001-3, Task-1001-4, Task-1001-5
   ```

4. Go to **Worker 4, 5 dashboards**
5. Click **"Task Configs Received"** tab
6. Verify tab is **EMPTY** (multicast disabled)

### Step 6: Command-Line Submission (Optional)

**Open new PowerShell:**
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Command line test task"
```

**Expected Output:**
```
Submitting task to broker: Command line test task
Task data sent to broker
Broker response: TASK_ACCEPTED:1002
Task submitted successfully with ID: 1002
```

---

## Expected Console Outputs

### Broker Console (After Task Submission)
```
M1: Originator connected. Handing off to Executor...
M1: Received task data: TaskID:1001 | Name:Customer Analytics Processing | Data:Process batch 1 | Process batch 2 | Process batch 3 | Process batch 4 | Process batch 5 | SubTasks:5
TaskManager: New Task created with ID: 1001. Split into 5 sub-tasks.
M1: Task accepted with ID: 1001

========================================
M2: Starting multi-threaded task processing
M2: Task ID: 1001
M2: Task Data: Process batch 1 | Process batch 2 | Process batch 3 | Process batch 4 | Process batch 5
========================================
M4: Broadcasting task config to multicast group...
M4: Task config broadcast complete
M2: Waiting 100ms for multicast propagation...
M2: Splitting task into 5 sub-tasks...
M2:   - Sub-task 1 created
M2:   - Sub-task 2 created
M2:   - Sub-task 3 created
M2:   - Sub-task 4 created
M2:   - Sub-task 5 created
M2: Thread 1 created for sub-task dispatch to worker 127.0.0.1:6002
M2: Thread 2 created for sub-task dispatch to worker 127.0.0.1:6001
M2: [Thread pool-1-thread-2] Dispatching sub-task 1 to worker 127.0.0.1:6002
M2: [Thread pool-1-thread-3] Dispatching sub-task 2 to worker 127.0.0.1:6001
...
M2: All 5 sub-tasks submitted to ExecutorService
========================================
```

### Worker Console (M4 Enabled - Workers 1-3)
```
M4: Multicast listener started on 230.0.0.1:6005
M4: Received task config: TASKCONFIG:1001:Customer Analytics Processing:5:Process batch 1 | Process batch 2 | Process batch 3 | Process batch 4 | Process batch 5:SubTask-1/5:...|SubTask-2/5:...|...
M4: Stored config for task 1001 (5 splits, 5 sub-tasks)
Worker: Received sub-task command: SUBTASK:1:Part of task 1001
Worker: Processing sub-task...
Worker: Sub-task completed. Sending ACK...
Worker: Sent ACK to broker: ACK:SUBTASK:1:COMPLETE
```

### Worker Console (M4 Disabled - Workers 4-5)
```
(No M4 output - multicast listener not started)
Worker: Received sub-task command: SUBTASK:1:Part of task 1001
Worker: Processing sub-task...
Worker: Sub-task completed. Sending ACK...
Worker: Sent ACK to broker: ACK:SUBTASK:1:COMPLETE
```

### Dashboard Browser (Real-Time Updates)
```
Broadcast Log (Live):
[18:05:32] PROGRESS:1001:Processing task 1001
[18:05:34] PROGRESS:1001:Task 1001 - 3/5 sub-tasks complete
[18:05:36] PROGRESS:1001:Task 1001 completed successfully
```

---

## Verification Checklist

After completing all steps, verify:

- [ ] Broker started without errors
- [ ] 5 workers connected successfully
- [ ] M5 UDP heartbeats appearing every 2 seconds
- [ ] M3 NIO broadcasts received by all workers
- [ ] **M4 enabled on Workers 1-3** (checkbox checked, status 🟢 ENABLED)
- [ ] **M4 disabled on Workers 4-5** (checkbox unchecked, status 🔴 DISABLED)
- [ ] Web UI accessible at http://localhost:8080
- [ ] Worker dashboards accessible at http://localhost:7000-7004
- [ ] Dashboard shows "WebSocket: Connected"
- [ ] Task submission via web returns success
- [ ] **M4 config appears in Workers 1-3 "Task Configs Received" tab**
- [ ] **M4 config NOT in Workers 4-5 tab (empty)**
- [ ] Broker console shows M1→M4→M2 task flow
- [ ] Workers show sub-task processing
- [ ] Dashboard broadcast log updates in real-time
- [ ] Recent tasks appear in index.html
- [ ] All module health indicators green

---

## Troubleshooting

### Port 8080 already in use
```powershell
Get-NetTCPConnection -LocalPort 8080 | Stop-Process -Force
```

### Worker won't connect
1. Check broker is running
2. Verify worker ID is 1-5
3. Rebuild: `mvn clean package`

### Dashboard not updating
1. Check browser console for errors (F12)
2. Verify WebSocket shows "Connected"
3. Refresh page (Ctrl+F5)

### Task submission fails
1. Check broker console for errors
2. Verify all 5 workers are running
3. Check task data is not empty

---

## Module Testing Checklist

### M1 - TCP Task Receiver ✅
- [ ] Broker listening on port 5000
- [ ] Originator can connect and send task
- [ ] Broker responds with TASK_ACCEPTED:ID

### M2 - Multi-threading ✅
- [ ] Task split into 5 sub-tasks
- [ ] All 5 workers receive sub-tasks concurrently
- [ ] All threads show in console logs
- [ ] All sub-tasks acknowledged

### M3 - Java NIO ✅
- [ ] Workers connect on port 5002
- [ ] Broadcasts appear every 2 seconds
- [ ] All workers receive broadcasts
- [ ] Dashboard shows broadcasts in real-time

### M4 - HTTP Data Loading ✅
- [ ] Javalin server starts on port 8080
- [ ] Static files served from /public
- [ ] POST /api/submit-task accepts JSON
- [ ] GET /api/load-task fetches external data

### M5 - UDP Heartbeats ✅
- [ ] Workers send UDP registration
- [ ] Broker receives on port 5001
- [ ] Heartbeats appear every 2 seconds
- [ ] Worker count accurate in dashboard

---

## Success Criteria

If you see ALL of these, everything works perfectly:

1. ✅ Broker console shows "Broker Server started successfully!"
2. ✅ 5 worker consoles show "M3: NIO connection established!"
3. ✅ Dashboard shows 5 active workers
4. ✅ Broadcast log shows messages every 2 seconds
5. ✅ Task submission returns success response
6. ✅ Worker consoles show sub-task processing
7. ✅ All module health indicators green
8. ✅ No error messages in any console

---

## Quick Commands Reference

### Kill All Java Processes (If Needed)
```powershell
Get-Process java | Stop-Process -Force
```

### Rebuild Project
```powershell
mvn clean package -DskipTests
```

### Check Port Usage
```powershell
Get-NetTCPConnection -LocalPort 5000,5001,5002,6000,8080
```

### View Compiled JAR
```powershell
dir target/*.jar
```

---

## Demo Script (For Presentation)

"Let me demonstrate our distributed task broker system with all 5 modules:

1. **[Start Broker]** - The broker orchestrates all components
2. **[Start Workers]** - 5 workers register via UDP and connect via NIO
3. **[Open Dashboard]** - Real-time monitoring shows live system status
4. **[Submit Task]** - Web interface triggers M1 TCP submission
5. **[Watch Processing]** - M2 splits into 5 sub-tasks, dispatched concurrently
6. **[Monitor Broadcasts]** - M3 NIO updates dashboard every 2 seconds
7. **[Verify Completion]** - All workers acknowledge, task completes

All 5 modules working together seamlessly!"

---

*Testing Guide Version: 1.0*  
*Last Updated: 2025-11-11*
