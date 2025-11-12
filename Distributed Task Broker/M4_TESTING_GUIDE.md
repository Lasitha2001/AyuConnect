# M4 Multicast Testing Guide

## Quick Test: Verify M4 Multicast Implementation

### Prerequisites
✅ Build successful: `mvn clean package -DskipTests`  
✅ JAR exists: `target/ComputeNet-Project-1.0.jar`

---

## Test 1: Start Broker and Verify M4 Initialization

### Command
```powershell
java -cp target/ComputeNet-Project-1.0.jar com.computenet.broker.server.BrokerServer
```

### Expected Output (Look for M4 line)
```
M1: TCP Task Receiver listening on port 5000
M5: UDP Listener started on port 5001
M3: NIO Selector started on port 5002
M4: Multicast initialized on 230.0.0.1:6005  ⭐ THIS LINE
...
Broker Server started successfully!
  TCP Task Receiver: port 5000
  UDP Worker Listener: port 5001
  NIO Broadcast Handler: port 5002
  Multicast Task Config: 230.0.0.1:6005      ⭐ THIS LINE
  Web UI: http://localhost:8080
```

✅ **Pass Criteria**: M4 initialization message appears

---

## Test 2: Start Worker and Verify M4 Multicast Join

### Command (Terminal 2)
```powershell
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6000
```

### Expected Output (Look for M4 line)
```
Worker Client starting...
Worker: Web interface started on http://localhost:7000
Worker: TCP Server listening on port 6000 for sub-tasks
Worker: Sent registration to broker
Worker: Registration acknowledged: REGISTERED
M4: Worker joined multicast group 230.0.0.1:6005  ⭐ THIS LINE
Worker: Connected to NIO broadcast channel
```

✅ **Pass Criteria**: "M4: Worker joined multicast group" appears

---

## Test 3: Start Second Worker

### Command (Terminal 3)
```powershell
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6001
```

### Expected Output
```
M4: Worker joined multicast group 230.0.0.1:6005  ⭐
```

✅ **Pass Criteria**: Both workers join the same multicast group

---

## Test 4: Submit Task and Verify M4 Broadcast

### Option A: Via Web UI
1. Open browser: `http://localhost:8080/index.html`
2. Fill in form:
   - **Task ID**: `1001` (number)
   - **Task Name**: `Test M4 Multicast`
   - **Number of Sub-Tasks**: Select `2 sub-tasks` from dropdown
   - **Sub-task 1 Data**: `Process data d1`
   - **Sub-task 2 Data**: `Process data d2`
   (Note: Sub-task input fields appear when you select sub-task count)
3. Click **"Submit Task via M1 TCP"**

### Option B: Via Command (Terminal 4)
```powershell
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "TaskID:M4TEST | Name:Multicast Test | Data:Process data d1 | SubTasks:2"
```

---

## Test 4: Expected Console Output

### Broker Console (Terminal 1)
Look for this sequence:
```
========================================
M2: Starting multi-threaded task processing
M2: Task ID: 1001
M2: Task Name: Multicast Test
M2: Task Data: Process data d1
M2: Sub-tasks to create: 2
========================================
M3: NIO handler notified of new task 1001

M4: Broadcasted config for task 1001 (splits: 2)      ⭐ MULTICAST SENT
M4: Task configuration broadcasted successfully        ⭐
M4: Wait complete - workers ready                      ⭐

M2: Splitting task into 2 sub-tasks...
M2:   - Sub-task 1 created
M2:   - Sub-task 2 created
M2: Thread 1 created for sub-task dispatch to worker localhost:6000
M2: Thread 2 created for sub-task dispatch to worker localhost:6001
```

### Worker 1 Console (Terminal 2)
```
M4: Received task config via multicast                 ⭐ MULTICAST RECEIVED
M4:   Task ID: 1001                                    ⭐
M4:   Split Count: 2                                   ⭐
M4:   Task Data: Process data d1                       ⭐

Worker: Broker connected to send sub-task
========================================
Worker: Received sub-task from broker
Worker: Sub-task data: TASK:1001:SUBTASK:1:SubTask-1/2: Process data d1 [Partition 1]
========================================
```

### Worker 2 Console (Terminal 3)
```
M4: Received task config via multicast                 ⭐ MULTICAST RECEIVED
M4:   Task ID: 1001                                    ⭐
M4:   Split Count: 2                                   ⭐
M4:   Task Data: Process data d1                       ⭐

Worker: Broker connected to send sub-task
========================================
Worker: Received sub-task from broker
Worker: Sub-task data: TASK:1001:SUBTASK:2:SubTask-2/2: Process data d1 [Partition 2]
========================================
```

---

## Test 5: Verify Timing (Config Before Dispatch)

### What to Look For:
The sequence MUST be:
1. ✅ M4: Broadcasted config (Broker)
2. ✅ M4: Received config (Workers)
3. ✅ M4: Wait complete (Broker)
4. ✅ M2: Dispatching sub-task (Broker)
5. ✅ Worker: Received sub-task (Workers)

**Critical**: Workers MUST receive M4 config BEFORE receiving M2 sub-task

---

## Test 6: Multi-Worker Broadcast Test

### Start 5 Workers
```powershell
# Terminal 2-6
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6000
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6001
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6002
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6003
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6004
```

### Submit Task with 5 Sub-tasks
1. Open browser: `http://localhost:8080/index.html`
2. Fill in form:
   - **Task ID**: `1002` (number)
   - **Task Name**: `Large Dataset Processing`
   - **Number of Sub-Tasks**: Select `5 sub-tasks` from dropdown
   - **Sub-task 1 Data**: `Dataset batch 1`
   - **Sub-task 2 Data**: `Dataset batch 2`
   - **Sub-task 3 Data**: `Dataset batch 3`
   - **Sub-task 4 Data**: `Dataset batch 4`
   - **Sub-task 5 Data**: `Dataset batch 5`
3. Click **"Submit Task via M1 TCP"**

### Expected Result
✅ **ALL 5 workers** receive M4 multicast config simultaneously  
✅ Broker sends **ONE multicast packet** (not 5 separate messages)  
✅ Sub-tasks dispatched via M2 TCP after 100ms wait

---

## Verification Checklist

### M4 Functionality
- [ ] Broker initializes TaskConfigMulticaster on startup
- [ ] Workers join multicast group 230.0.0.1:6005
- [ ] Multicast broadcast occurs BEFORE sub-task dispatch
- [ ] All workers receive config simultaneously
- [ ] Message format is correct: `TASKCONFIG:taskId:splitCount:data`
- [ ] 100ms wait happens after broadcast
- [ ] Workers log received config with M4: prefix

### Integration with Other Modules
- [ ] M1: Task submission still works via TCP 5000
- [ ] M2: Sub-task dispatch still works via TCP 6000-6004
- [ ] M3: NIO broadcasts still work on port 5002
- [ ] M5: Worker registration still works via UDP 5001

### No Regressions
- [ ] `/api/load-task` endpoint removed (expected)
- [ ] testHttpDataLoading() method removed (expected)
- [ ] Web UI still accessible at localhost:8080
- [ ] Worker web UIs still accessible at 7000-7004
- [ ] Task completion notifications still work

---

## Common Issues & Solutions

### Issue 1: "M4: Worker joined multicast group" not appearing
**Solution**: 
- Check firewall settings (allow UDP 6005)
- Ensure network interface supports multicast
- Try running with admin privileges

### Issue 2: Workers receive sub-task but not config
**Cause**: Race condition - 100ms wait too short  
**Solution**: Already implemented - config sent before dispatch

### Issue 3: Multicast not working on Windows
**Solution**: 
- Enable "IP Helper" service
- Check `ipconfig /all` for multicast support
- Try different multicast address (224.0.0.1)

### Issue 4: "Multicast listener error" in worker
**Solution**:
- Check if port 6005 is already in use
- Verify localhost network interface exists
- Run `netstat -an | findstr 6005`

---

## Success Criteria

✅ **M4 Multicast is working correctly if**:

1. Broker console shows:
   - "M4: Multicast initialized on 230.0.0.1:6005"
   - "M4: Broadcasted config for task X (splits: N)"
   - "M4: Task configuration broadcasted successfully"
   - "M4: Wait complete - workers ready"

2. Worker consoles show (ALL workers):
   - "M4: Worker joined multicast group 230.0.0.1:6005"
   - "M4: Received task config via multicast"
   - "M4:   Task ID: X"
   - "M4:   Split Count: N"
   - "M4:   Task Data: ..."

3. Timing is correct:
   - Config received BEFORE sub-task
   - 100ms wait completes before dispatch

4. No errors in any console

---

## Performance Test

### Measure Multicast Efficiency

#### With M4 Multicast (Current)
- **Workers**: 5
- **Broadcasts**: 1 (multicast to all)
- **Network packets**: 1 UDP multicast

#### Without M4 Multicast (Hypothetical)
- **Workers**: 5
- **Broadcasts**: 5 (unicast to each)
- **Network packets**: 5 separate UDP packets

**Improvement**: 5x reduction in network traffic! 🚀

---

## Cleanup

```powershell
# Stop all Java processes
Get-Process java | Stop-Process -Force
```

---

## Next Test: Full Integration

Once M4 is verified, test complete workflow:
1. Start broker
2. Start 5 workers
3. Submit 10 tasks (2 sub-tasks each)
4. Verify all 20 sub-tasks delivered
5. Check M4 broadcasts happened 10 times
6. Complete tasks via worker web UIs
7. Verify broker updates statistics

**Expected**: All M1-M5 working together seamlessly! ✅

---

**Date**: November 12, 2025  
**M4 Status**: ✅ READY FOR TESTING  
**Build Status**: ✅ SUCCESS
