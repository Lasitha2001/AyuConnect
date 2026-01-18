# Getting Started - Distributed Task Broker System

## 📋 Welcome!

This guide will help you get the Distributed Task Broker System up and running in **under 5 minutes**. Whether you're a student, educator, or developer interested in distributed systems, this step-by-step guide is designed for absolute beginners.

---

## 🎯 What You'll Achieve

By the end of this guide, you will have:
- ✅ Built the entire project from source
- ✅ Started a broker server handling 5 network protocols
- ✅ Launched 5 worker nodes in a distributed network
- ✅ Submitted tasks and watched them execute in parallel
- ✅ Monitored real-time progress through web dashboards

---

## ⚙️ Prerequisites Check

### Required Software

#### 1. Java 17 or Higher
**Check if installed**:
```powershell
java -version
```

**Expected output**:
```
java version "17.0.x" or higher
```

**Not installed?** Download from: https://adoptium.net/

---

#### 2. Maven 3.6 or Higher
**Check if installed**:
```powershell
mvn -version
```

**Expected output**:
```
Apache Maven 3.6.x or higher
```

**Not installed?** Download from: https://maven.apache.org/download.cgi

**Installation tip**: Add Maven's `bin` directory to your PATH environment variable.

---

#### 3. Windows PowerShell
**Check version**:
```powershell
$PSVersionTable.PSVersion
```

**Expected**: Version 5.1 or higher (comes with Windows 10/11)

---

### System Requirements
- **OS**: Windows 10/11 (scripts use PowerShell; Linux/Mac users can adapt)
- **RAM**: Minimum 4GB, recommended 8GB
- **Disk Space**: ~500MB for project + dependencies
- **Network**: Ports 5000-5004, 6000-6004, 7000-7004, 8080 must be available

---

## 🚀 Step-by-Step Setup

### Step 1: Navigate to Project Directory

Open PowerShell and navigate to the project folder:

```powershell
cd "C:\Users\Lasitha Hasaranga\OneDrive\Desktop\AyuConnect\AyuConnect\Distributed Task Broker"
```

**Verify you're in the right place**:
```powershell
ls pom.xml
```

You should see the `pom.xml` file listed.

---

### Step 2: Build the Project (30 seconds)

Run the Maven build command:

```powershell
mvn clean package -DskipTests
```

**What's happening?**
- Maven downloads dependencies (first time only)
- Compiles all Java source files
- Packages everything into a single JAR file

**Expected output** (at the end):
```
[INFO] BUILD SUCCESS
[INFO] Total time: 30-60 seconds
```

**Verify the JAR was created**:
```powershell
ls target\ComputeNet-Project-1.0.jar
```

**Troubleshooting**:
- If build fails, ensure Java 17 is installed: `java -version`
- If Maven not found, check PATH environment variable
- If dependencies fail to download, check internet connection

---

### Step 3: Start the Broker Server

Run the broker startup script:

```powershell
.\start-broker.ps1
```

**What you'll see**:
```
========================================
Starting Broker Server...
========================================

Checking port availability...
All ports available!

Modules Starting:
  - M1: TCP Task Receiver (port 5000)
  - M2: Multi-threading Executor (10 threads)
  - M3: NIO Broadcast Handler (port 5002)
  - M4: Multicast Config Broadcaster (230.0.0.1:6005)
  - M5: UDP Worker Listener (port 5001)

Web Interfaces:
  - Broker UI:   http://localhost:8080
  - Submit Task: http://localhost:8080/index.html
  - Dashboard:   http://localhost:8080/dashboard.html

Press Ctrl+C to stop the server
========================================

[BrokerServer] All modules started successfully
[BrokerServer] Web UI: http://localhost:8080
```

**What's running now?**
- TCP server on port 5000 (task submission)
- UDP server on port 5001 (worker registration)
- NIO server on port 5002 (worker communication)
- Multicast broadcaster on 230.0.0.1:6005
- Web server on port 8080 (dashboards)

**Keep this terminal window open!** The broker needs to stay running.

**Troubleshooting**:
- **"Port already in use"**: Run `.\cleanup-ports.ps1` first
- **"Could not find main class"**: Re-run `mvn clean package`

---

### Step 4: Start Worker Clients

**Open a NEW PowerShell window** (don't close the broker window!)

Navigate to the same directory:
```powershell
cd "C:\Users\Lasitha Hasaranga\OneDrive\Desktop\AyuConnect\AyuConnect\Distributed Task Broker"
```

Run the worker startup script:
```powershell
.\start-workers.ps1
```

**What happens?**
- 5 new terminal windows will open automatically
- Each window runs one worker instance
- Workers automatically register with the broker via UDP

**In each worker window, you'll see**:
```
Worker 1 (TCP:6000, HTTP:7000)
Worker-1 started. Listening on port 6000
Sending UDP registration to broker...
Registered with broker: REGISTERED
Connecting to NIO broadcast channel...
NIO channel connected
Web interface: http://localhost:7000
```

**You now have**:
- 1 broker server (original window)
- 5 worker clients (5 new windows)
- All 6 components communicating!

**Worker ports**:
| Worker | TCP Port | HTTP Port |
|--------|----------|-----------|
| Worker 1 | 6000 | 7000 |
| Worker 2 | 6001 | 7001 |
| Worker 3 | 6002 | 7002 |
| Worker 4 | 6003 | 7003 |
| Worker 5 | 6004 | 7004 |

---

### Step 5: Submit Your First Task! 🎉

Open your web browser and navigate to:
```
http://localhost:8080/index.html
```

**Fill in the form**:

1. **Task ID**: `1001` (any number)
2. **Task Name**: `My First Distributed Task`
3. **Number of Sub-Tasks**: Select `5 sub-tasks` from dropdown
4. **Sub-task data fields** (will appear dynamically):
   - Sub-task 1 Data: `Process batch A`
   - Sub-task 2 Data: `Process batch B`
   - Sub-task 3 Data: `Process batch C`
   - Sub-task 4 Data: `Process batch D`
   - Sub-task 5 Data: `Process batch E`

5. Click **"Submit Task via M1 TCP"**

**Success!** You should see:
```
✅ Task submitted successfully!
Task ID: 1001
Sub-tasks: 5
```

---

### Step 6: Monitor Real-Time Progress

Open a new browser tab:
```
http://localhost:8080/dashboard.html
```

**What you'll see**:
- **WebSocket Status**: 🟢 Connected
- **Active Workers**: 5
- **Pending Tasks**: Updates in real-time
- **Completed Tasks**: Increments as tasks finish
- **M3 NIO Broadcasts**: Live updates every 2 seconds
- **Module Health**: All 5 modules (M1-M5) showing green

**Watch the magic happen!**
- Task gets decomposed into 5 sub-tasks (M2)
- Sub-tasks broadcast to workers (M3)
- Workers process in parallel
- Progress updates every 2 seconds
- Dashboard shows real-time status

---

### Step 7: Explore Worker Dashboards

Each worker has its own web interface. Open any of these URLs:

- Worker 1: http://localhost:7000
- Worker 2: http://localhost:7001
- Worker 3: http://localhost:7002
- Worker 4: http://localhost:7003
- Worker 5: http://localhost:7004

**What you'll see** (per worker):
- Worker ID and connection status
- Current state: Idle / Processing
- List of sub-tasks received
- **M4 Multicast Control**: Enable/Disable checkbox
- Task configurations received (when multicast enabled)

**Try the Multicast Feature**:
1. On Worker 1 dashboard, check "Enable Multicast Task Config Receiver"
2. Submit a new task from http://localhost:8080/index.html
3. Go to Worker 1's "Task Configs Received" tab
4. You'll see the task configuration broadcasted via M4!

---

## 🎓 What Just Happened? (The Magic Explained)

### Behind the Scenes

1. **Task Submission (M1 - TCP)**:
   - Your browser sent task data to broker via TCP (port 5000)
   - TCP ensures reliable delivery (no data loss)
   - Broker acknowledged: "TASK_ACCEPTED:1001"

2. **Task Decomposition (M2 - Multi-threading)**:
   - Broker queried M5: "How many workers?" → Answer: 5
   - Task split into 5 sub-tasks (one per worker)
   - ExecutorService thread handled this concurrently

3. **Multicast Broadcast (M4 - Optional)**:
   - If workers have multicast enabled, they receive task config
   - Sent to multicast group 230.0.0.1:6005
   - Workers see: "TASKCONFIG:1001:TaskName:5:..."

4. **Sub-task Distribution (M3 - NIO)**:
   - Non-blocking I/O (NIO) broadcast sub-tasks to all workers
   - Single thread handles all 5 worker connections efficiently
   - Workers receive via port 5002

5. **Progress Updates (M3 - NIO)**:
   - Every 2 seconds: "Progress: 60% (3/5 completed)"
   - WebSocket sends to dashboard for real-time display

6. **Result Collection**:
   - Workers send results back via NIO channel
   - Broker aggregates results
   - Dashboard updates: "Completed Tasks: 1"

---

## 🔍 Verification Checklist

Ensure everything is working:

### Broker Server
- [ ] Terminal shows "All modules started successfully"
- [ ] http://localhost:8080 loads (shows index.html)
- [ ] http://localhost:8080/dashboard.html loads

### Workers
- [ ] 5 terminal windows are open
- [ ] Each shows "Registered with broker: REGISTERED"
- [ ] Each shows "NIO channel connected"
- [ ] Worker dashboards load (ports 7000-7004)

### Task Submission
- [ ] Can submit task via web UI
- [ ] See green success message
- [ ] Dashboard shows task in queue
- [ ] Task completes (workers process sub-tasks)

### Communication
- [ ] Dashboard shows "Active Workers: 5"
- [ ] M3 NIO broadcasts appear every 2 seconds
- [ ] All module health indicators are green

---

## 🧪 Next Steps - Testing

### Try Different Scenarios

#### 1. Multiple Tasks
Submit 3-5 tasks in quick succession:
- Tasks will queue and process in order
- Watch pending count increase/decrease
- See concurrent processing in action

#### 2. Command Line Submission
Open a new PowerShell window:
```powershell
cd "Distributed Task Broker"
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "CLI test task"
```

#### 3. Test Individual Modules
Run automated tests:
```powershell
.\test-m1-tcp-submission.ps1
.\test-m2-multithreading.ps1
.\test-m3-nio-broadcast.ps1
.\test-m4-multicast.ps1
.\test-m5-udp-registration.ps1
```

#### 4. Experiment with Worker Count
- Close 2-3 worker windows (Ctrl+C)
- Submit new task with fewer sub-tasks
- Watch task adapt to available workers

---

## 🛑 Shutting Down

### Graceful Shutdown

1. **Stop submitting new tasks**

2. **Stop workers**:
   - In each worker window: Press `Ctrl+C`
   - Or close the terminal windows

3. **Stop broker**:
   - In broker window: Press `Ctrl+C`
   - Wait for "Shutting down Broker Server..." message

4. **Clean up ports** (if needed):
   ```powershell
   .\cleanup-ports.ps1
   ```

---

## 🐛 Common Issues & Solutions

### Issue 1: Ports Already in Use

**Symptom**:
```
ERROR: The following ports are already in use:
  - Port 5000
```

**Solution**:
```powershell
.\cleanup-ports.ps1
```

This script kills all processes using project ports.

---

### Issue 2: Workers Not Registering

**Symptom**: Dashboard shows "Active Workers: 0"

**Check**:
1. Broker is running (check terminal)
2. Worker windows show "REGISTERED" message
3. No firewall blocking UDP port 5001

**Solution**:
```powershell
# Restart everything
.\cleanup-ports.ps1
.\start-broker.ps1
# In new window:
.\start-workers.ps1
```

---

### Issue 3: Web UI Not Loading

**Symptom**: Browser shows "Can't reach this page"

**Check**:
1. Broker terminal shows "Web UI: http://localhost:8080"
2. No other application using port 8080
3. Try different browser

**Solution**:
```powershell
# Check if broker is running
netstat -ano | findstr :8080

# If port in use by another app:
.\cleanup-ports.ps1
.\start-broker.ps1
```

---

### Issue 4: Maven Build Fails

**Symptom**:
```
[ERROR] Failed to execute goal...
```

**Solutions**:
```powershell
# Update Maven dependencies
mvn clean install -U

# Verify Java version
java -version  # Must be 17+

# Check Maven version
mvn -version   # Must be 3.6+
```

---

## 📚 Additional Resources

### Documentation
- **README.md** - Complete system documentation
- **PROJECT_SUMMARY.md** - High-level overview and architecture
- **QUICK_TEST_GUIDE.md** - 5-minute comprehensive test
- **M1-M5 Testing Guides** - Individual module testing
- **PROJECT_REPORT.md** - Full technical report

### Learning Materials
- **MODULE-REFERENCE.md** - Detailed module APIs
- **INTEGRATION_COMPLETE.md** - Integration patterns
- **PRESENTATION_GUIDE.md** - Demo walkthrough

---

## 🎯 Learning Objectives

By working with this system, you'll learn:

### Network Programming
- ✅ TCP socket programming (blocking I/O)
- ✅ UDP datagram communication
- ✅ Java NIO (non-blocking I/O)
- ✅ IP Multicast (one-to-many)
- ✅ WebSocket (real-time web)

### Concurrent Programming
- ✅ Thread pools (ExecutorService)
- ✅ Lock-free data structures (ConcurrentHashMap)
- ✅ Event-driven architecture (NIO Selector)
- ✅ Thread-safe design patterns

### Distributed Systems
- ✅ Task decomposition and distribution
- ✅ Worker discovery and registration
- ✅ Load balancing strategies
- ✅ Real-time monitoring and observability

### Web Development
- ✅ REST API design
- ✅ WebSocket communication
- ✅ Real-time dashboards
- ✅ Responsive UI/UX

---

## ✅ Success Criteria

You've successfully set up the system when:

- [x] Broker server starts without errors
- [x] All 5 workers register successfully
- [x] Dashboard shows 5 active workers
- [x] Can submit tasks via web UI
- [x] Tasks complete and show on dashboard
- [x] All module health indicators are green
- [x] Worker dashboards are accessible
- [x] Real-time updates work (2-second broadcasts)

---

## 🎉 Congratulations!

You've successfully:
- ✅ Built a distributed computing system
- ✅ Deployed 5 network protocols
- ✅ Launched a multi-node cluster
- ✅ Submitted and processed distributed tasks
- ✅ Monitored real-time system status

**Next Steps**:
1. Explore the code in `src/main/java/com/computenet/`
2. Read module-specific testing guides (M1-M5)
3. Experiment with task submission patterns
4. Review the architectural documentation
5. Try modifying configuration parameters

---

## 📞 Need Help?

1. **Check Troubleshooting** section above
2. **Review logs** in broker/worker terminals
3. **Examine browser console** (F12) for web issues
4. **Read module guides** for specific components
5. **Check PROJECT_REPORT.md** for technical details

---

## 🔗 Quick Reference URLs

Once running, bookmark these:

| Interface | URL | Purpose |
|-----------|-----|---------|
| Task Submission | http://localhost:8080/index.html | Submit new tasks |
| Broker Dashboard | http://localhost:8080/dashboard.html | Monitor system |
| Worker 1 Dashboard | http://localhost:7000 | View Worker 1 status |
| Worker 2 Dashboard | http://localhost:7001 | View Worker 2 status |
| Worker 3 Dashboard | http://localhost:7002 | View Worker 3 status |
| Worker 4 Dashboard | http://localhost:7003 | View Worker 4 status |
| Worker 5 Dashboard | http://localhost:7004 | View Worker 5 status |
| API Endpoint | http://localhost:8080/api/workers | Get worker list (JSON) |

---

**Built with ❤️ for Distributed Systems Learning**

**Last Updated**: January 2026  
**Version**: 1.0  
**Difficulty**: Beginner-Friendly
