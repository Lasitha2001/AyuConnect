# Distributed Task Broker - Comprehensive Testing Guide

## 📚 Testing Documentation Structure

This is the **master testing guide** that summarizes all M1-M5 module tests and provides cross-module testing scenarios.

### Individual Module Test Guides

- **M1-TCP-TESTING-GUIDE.md** - TCP Task Submission testing
- **M2-MULTITHREADING-TESTING-GUIDE.md** - Multi-threading and ExecutorService testing
- **M3-NIO-TESTING-GUIDE.md** - Java NIO broadcast testing
- **M4_TESTING_GUIDE.md** - Multicast manual control testing
- **M5-UDP-TESTING-GUIDE.md** - UDP heartbeat testing
- **QUICK_TEST_GUIDE.md** - Fast 5-minute integration test

---

## 🎯 Module Summary

| Module | Technology | Port | Key Feature | Test Focus |
|--------|-----------|------|-------------|------------|
| M1 | TCP Sockets | 5000 | Reliable task submission | Blocking I/O, acknowledgment protocol |
| M2 | ExecutorService | N/A | Multi-threaded dispatch | Concurrent sub-task processing |
| M3 | Java NIO | 5002 | Non-blocking broadcasts | Selector pattern, 2-second intervals |
| M4 | UDP Multicast | 6005 | Manual multicast control | Opt-in subscription, localStorage persistence |
| M5 | UDP Datagram | 5001 | Worker registration | Heartbeat monitoring |

---

## 🚀 Quick Start - Full System Test

### Prerequisites
```powershell
# Build project
mvn clean package -DskipTests

# Verify JAR exists
ls target\ComputeNet-Project-1.0.jar

# If you get "Port already in use" errors
.\cleanup-ports.ps1
```

### Start System (6 terminals)

**Terminal 1 - Broker:**
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.broker.server.BrokerServer
```

**Terminals 2-6 - Workers:**
```powershell
# Worker 1
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 1

# Worker 2
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 2

# Worker 3
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 3

# Worker 4
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 4

# Worker 5
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 5
```

---

## 🧪 Cross-Module Testing Scenarios

### Scenario 1: M1 + M4 + M2 Integration

**Purpose:** Test task submission with multicast config broadcast

**Steps:**
1. Start broker + 5 workers
2. Enable M4 on Workers 1-3 (checkbox in dashboard)
3. Submit task via M1 (OriginatorClient or Web UI)
4. **Verify M4**: Workers 1-3 show config in "Task Configs Received" tab
5. **Verify M4**: Workers 4-5 have empty tab (disabled)
6. **Verify M2**: All 5 workers receive sub-tasks via TCP
7. **Verify M1**: Broker shows task acknowledgment

**Expected Flow:**
```
User → M1 TCP (5000) → M4 Multicast Broadcast → M2 Split & Dispatch
                           ↓
                    Workers 1-3: Receive config
                    Workers 4-5: Ignore (disabled)
                           ↓
                    All workers: Receive sub-tasks
```

### Scenario 2: M3 + M4 + M5 Integration

**Purpose:** Test real-time monitoring with multicast and heartbeats

**Steps:**
1. Start broker + 5 workers
2. Enable M4 on Worker 1 only
3. Submit task
4. **Verify M3**: All workers receive NIO broadcasts every 2 seconds
5. **Verify M4**: Only Worker 1 shows config in tab
6. **Verify M5**: Broker logs show heartbeats from all 5 workers

**Console Output (Worker 1):**
```
M5: Sending heartbeat to broker
M3: NIO: Received broadcast: PROGRESS:1001:Processing...
M4: Received task config: TASKCONFIG:1001:...
```

### Scenario 3: localStorage Persistence Test (M4)

**Purpose:** Test M4 state survives page refresh

**Steps:**
1. Start broker + Worker 1
2. Open Worker 1 dashboard: http://localhost:7000
3. **Check** M4 multicast checkbox → Status shows 🟢 ENABLED
4. **Refresh page** (F5)
5. **Verify**: Checkbox still checked, status still 🟢 ENABLED
6. **Uncheck** M4 checkbox → Status shows 🔴 DISABLED
7. **Refresh page** (F5)
8. **Verify**: Checkbox still unchecked, status still 🔴 DISABLED

**localStorage Key:** `worker_6000_m4_enabled`

### Scenario 4: Worker Reconnection Test (M3)

**Purpose:** Test M3 NIO auto-reconnect

**Steps:**
1. Start broker + Worker 1
2. **Stop broker** (Ctrl+C)
3. **Check Worker 1 console:**
   ```
   M3: NIO connection lost. Reconnecting in 2 seconds...
   M3: NIO connection failed. Retrying in 5 seconds...
   ```
4. **Restart broker**
5. **Check Worker 1 console:**
   ```
   M3: NIO connection established!
   NIO: Received broadcast: PROGRESS:0:No active task
   ```

### Scenario 5: Multi-Worker M4 Configuration

**Purpose:** Test independent M4 states across workers

**Steps:**
1. Start broker + 5 workers
2. **Configure M4 states:**
   - Worker 1: ☑ ENABLED
   - Worker 2: ☑ ENABLED
   - Worker 3: ☐ DISABLED
   - Worker 4: ☑ ENABLED
   - Worker 5: ☐ DISABLED
3. Submit task via Web UI
4. **Verify M4 configs received:**
   - Workers 1, 2, 4: Show config in "Task Configs Received" tab
   - Workers 3, 5: Empty tab
5. **Verify all workers:** Receive sub-tasks in "Active Tasks" tab (M2)

---

## 📊 Module-Specific Testing

### M1: TCP Task Submission
**Guide:** M1-TCP-TESTING-GUIDE.md

**Key Tests:**
- Blocking TCP connection on port 5000
- Task acknowledgment protocol
- Integration with M2 ExecutorService
- **New**: M4 broadcast trigger on submission

**Test Command:**
```powershell
.\test-m1-tcp-submission.ps1
```

### M2: Multi-threading
**Guide:** M2-MULTITHREADING-TESTING-GUIDE.md

**Key Tests:**
- ExecutorService 10-thread pool
- Concurrent sub-task dispatch
- TCP connections to workers (6000-6004)
- **New**: Waits 100ms after M4 broadcast

**Test Command:**
```powershell
.\test-m2-multithreading.ps1
```

**Verify:**
```
M2: Thread 1 created for sub-task dispatch to worker...
M2: All 5 sub-tasks submitted to ExecutorService
```

### M3: Java NIO Broadcasts
**Guide:** M3-NIO-TESTING-GUIDE.md

**Key Tests:**
- Selector pattern (non-blocking I/O)
- 2-second broadcast interval
- Worker auto-reconnect logic
- Integration with WebSocket for dashboard

**Test Command:**
```powershell
.\test-m3-nio-broadcast.ps1
```

**Verify:**
```
M3: NIO ServerSocketChannel bound to port 5002
M3: Selector detected X ready keys
M3: Broadcasting task progress update
```

### M4: Multicast Manual Control
**Guide:** M4_TESTING_GUIDE.md

**Key Tests:**
- Manual opt-in via checkbox
- localStorage persistence
- Enhanced message format (taskName + all sub-tasks)
- Independent worker states
- Default DISABLED state

**Test Command:**
```powershell
.\test-m4-multicast.ps1
```

**Message Format:**
```
TASKCONFIG:taskId:taskName:splitCount:data:subTask1|subTask2|...
```

**Verify:**
```
M4: Multicast listener started on 230.0.0.1:6005
M4: Received task config: TASKCONFIG:1001:...
M4: Stored config for task 1001 (N splits, N sub-tasks)
```

### M5: UDP Heartbeats
**Guide:** M5-UDP-TESTING-GUIDE.md

**Key Tests:**
- Worker registration via UDP
- Heartbeat every 5 seconds
- Non-blocking DatagramSocket

**Test Command:**
```powershell
.\test-m5-udp-registration.ps1
```

**Verify:**
```
M5: UDP listener started on port 5001
M5: Worker registered: [Address=/127.0.0.1, TcpPort=6000]
M5: Sending heartbeat to broker
```
---

## 🎯 Success Criteria

A successful test includes verification of:

- [x] **M1**: TCP task submission and acknowledgment
- [x] **M2**: Multi-threaded sub-task dispatch (10 threads)
- [x] **M3**: NIO broadcasts every 2 seconds
- [x] **M4**: Manual multicast control with localStorage persistence
  - Enabled workers receive configs
  - Disabled workers ignore multicast
  - State survives page refresh
- [x] **M5**: UDP worker registration and heartbeats
- [x] **Integration**: All modules work together seamlessly
- [x] **Web UI**: Broker and worker dashboards functional
- [x] **Auto-reconnect**: Workers recover from connection loss

---

## Troubleshooting

### Port Already in Use
```powershell
# Run the automated cleanup script
.\cleanup-ports.ps1

# Or manually find and kill process
netstat -ano | findstr ":5000"  # Find PID using port
taskkill /PID <PID> /F          # Kill process by PID
```

### M4 Multicast Not Receiving
- **Check**: Checkbox is checked and status shows 🟢 ENABLED
- **Verify**: localStorage value: `localStorage.getItem('worker_6000_m4_enabled')` should be `'true'`
- **Test**: Uncheck and re-check checkbox, then submit new task
- **Console**: Look for "M4: Multicast listener started on 230.0.0.1:6005"

### Worker Not Connecting to NIO
- **Check**: Broker console shows "M3: NIO ServerSocketChannel bound to port 5002"
- **Verify**: Worker console shows "M3: NIO connection established!"
- **Reconnect**: Workers retry every 2-5 seconds automatically

### Build Errors
```powershell
# Clean rebuild
mvn clean package -DskipTests

# Verify JAR
ls target\ComputeNet-Project-1.0.jar
```

---

## 📚 Documentation References

- **MODULE-REFERENCE.md** - Complete module overview and architecture
- **INTEGRATION_COMPLETE.md** - Full system workflow and integration details
- **QUICK_TEST_GUIDE.md** - Fast 5-minute test sequence
- **M1-TCP-TESTING-GUIDE.md** - M1 detailed testing
- **M2-MULTITHREADING-TESTING-GUIDE.md** - M2 detailed testing
- **M3-NIO-TESTING-GUIDE.md** - M3 detailed testing
- **M4_TESTING_GUIDE.md** - M4 manual control testing
- **M5-UDP-TESTING-GUIDE.md** - M5 heartbeat testing

---

## 🏆 System Status

**All 5 Modules:** ✅ Fully Implemented and Tested  
**Web Interface:** ✅ Complete (Broker + Worker Dashboards)  
**M4 Manual Control:** ✅ localStorage persistence working  
**Build Status:** ✅ SUCCESS  
**Production Ready:** ✅ Yes

---

*Last Updated: 2025-01-15*  
*Build Version: 1.0*  
*Java: 17 | Maven | Javalin 6.1.3*
2. Click "Mark as Complete" on all PENDING tasks
3. Watch status change to COMPLETED

**Verify:**
- ✅ All tasks eventually show COMPLETED
- ✅ Main dashboard shows 100% progress
- ✅ Workers Registry shows all workers back to IDLE

### Step 12: Test Auto-Refresh

**Keep worker dashboard open (http://localhost:7000)**

**In another tab, submit new task**

**Wait up to 5 seconds**

**Verify:**
- ✅ New task appears automatically (no manual refresh needed)
- ✅ Active Tasks counter updates
- ✅ Task card appears with PENDING status

## 🎯 Success Criteria

All tests pass if:

1. ✅ **Broker starts** without errors
2. ✅ **All 5 workers register** successfully
3. ✅ **Workers Registry** shows all workers with correct ports
4. ✅ **Individual dashboards** load on ports 7000-7004
5. ✅ **Task submission** creates sub-tasks
6. ✅ **Sub-tasks appear** on worker dashboards
7. ✅ **Manual completion** changes status from PENDING to COMPLETED
8. ✅ **Auto-refresh** updates pages without reload
9. ✅ **Navigation links** work between all pages
10. ✅ **M3 NIO broadcasts** continue working on dashboard

## ❌ Common Issues and Fixes

### Issue: Worker doesn't appear in registry

**Fix:**
- Check worker console for "Registration acknowledged"
- Restart worker if needed
- Wait 5 seconds for auto-refresh
- Manually refresh the page

### Issue: Task doesn't appear on worker dashboard

**Fix:**
- Check broker console to see which workers received tasks
- Verify you're looking at the correct worker dashboard
- Manually refresh or wait for auto-refresh
- Check worker console for "Sub-task stored with key X"

### Issue: Button stays disabled

**Fix:**
- Task is already completed (check badge color)
- Refresh page to see latest status

### Issue: Port already in use

**Fix:**
```powershell
# Kill all Java processes
Get-Process java | Stop-Process -Force
```

Then restart broker and workers.

### Issue: Worker web interface not accessible

**Fix:**
- Check worker console for "Web interface started on..."
- Verify worker is still running (check terminal window)
- Try accessing via IP: http://127.0.0.1:700X

## 📸 Screenshots You Should See

1. **Workers Registry**: Grid of 5 worker cards with purple gradient
2. **Worker Dashboard**: Clean white interface with task cards
3. **Task Card (Pending)**: Yellow badge, green button enabled
4. **Task Card (Completed)**: Green badge, gray button disabled
5. **Empty State**: Nice icon with "No Active Tasks" message

## 🎓 What You've Tested

- ✅ **M1 (TCP)**: Task submission still works
- ✅ **M2 (Multi-threading)**: Sub-task splitting still works
- ✅ **M3 (NIO)**: Progress broadcasts still work
- ✅ **M5 (UDP)**: Worker registration still works
- ✅ **M6 (HTTP Workers)**: New worker web interfaces work!
- ✅ **WebSocket**: Real-time dashboard updates work
- ✅ **REST APIs**: All endpoints returning correct JSON
- ✅ **Frontend**: All pages load and navigate properly

---

**If all tests pass, your enhanced distributed task broker is fully functional! 🎉**
