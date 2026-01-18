# Distributed Task Broker System

## 📋 Executive Summary

The **Distributed Task Broker System** is a sophisticated multi-protocol distributed computing platform built with Java 17 that demonstrates advanced network programming concepts. The system efficiently distributes computational tasks across multiple worker nodes using a combination of TCP, UDP, NIO (Non-blocking I/O), and IP Multicast protocols.

### What It Does
- **Accepts tasks** from originators via reliable TCP connections
- **Decomposes tasks** into parallel sub-tasks using multi-threading
- **Distributes work** across registered worker nodes automatically
- **Monitors progress** in real-time through web dashboards
- **Broadcasts updates** using non-blocking I/O and multicast protocols
- **Manages workers** via lightweight UDP registration and heartbeats

### Key Innovation
This system showcases five distinct network communication paradigms working together seamlessly, making it an excellent learning resource for distributed systems, network programming, and concurrent processing.

## 🏗️ Project Structure

```
Distributed Task Broker/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/computenet/
│   │   │   │   ├── broker/               # Broker Server Logic
│   │   │   │   │   ├── server/           # Core Network Components
│   │   │   │   │   │   ├── BrokerServer.java           (Main server orchestrator)
│   │   │   │   │   │   ├── TaskTcpReceiver.java        (M1: TCP Task Receiver)
│   │   │   │   │   │   ├── WorkerNIOHandler.java       (M3: NIO Broadcast Handler)
│   │   │   │   │   │   ├── WorkerUdpListener.java      (M5: UDP Worker Listener)
│   │   │   │   │   ├── service/          # Business Logic
│   │   │   │   │   │   ├── TaskManager.java            (In-Memory Task Management)
│   │   │   │   │   │   ├── HttpDataLoader.java         (M4: HTTP Data Loader)
│   │   │   │   ├── client/               # Client Applications
│   │   │   │   │   ├── WorkerClient.java               (Worker application)
│   │   │   │   │   ├── OriginatorClient.java           (Task submission client)
│   │   │   ├── App.java                  # Main Entry Point
│   │   │
│   │   ├── resources/
│   │   │   ├── public/                   # Frontend UI Files
│   │   │   │   ├── index.html            (Task submission UI)
│   │   │   │   ├── dashboard.html        (Broker dashboard UI)
│   │   │   │   ├── app.js                (WebSocket communication)
│   │   │   │   ├── style.css             (UI styling)
├── pom.xml                               # Maven configuration
└── README.md                             # This file
```

## 🚀 Quick Start Guide (3 Steps)

### Prerequisites
- **Java 17** or higher ([Download here](https://adoptium.net/))
- **Maven 3.6+** ([Download here](https://maven.apache.org/download.cgi))
- **Windows PowerShell** (for automated scripts)

### Step 1: Build the Project (30 seconds)

Open PowerShell in the `Distributed Task Broker` directory:

```powershell
# Navigate to project directory
cd "Distributed Task Broker"

# Build the JAR with all dependencies
mvn clean package -DskipTests
```

**Expected Output:** `BUILD SUCCESS` and `ComputeNet-Project-1.0.jar` in `target/` folder

---

### Step 2: Start the Broker Server

```powershell
.\start-broker.ps1
```

**What happens:**
- ✅ Checks ports 5000, 5001, 5002, 8080 are available
- ✅ Starts all 5 modules (M1-M5)
- ✅ Launches web interface on http://localhost:8080

**Wait for this confirmation:**
```
Web UI: http://localhost:8080
Dashboard: http://localhost:8080/dashboard.html
```

---

### Step 3: Start Worker Clients (5 workers)

**Open a new PowerShell window** in the same directory:

```powershell
.\start-workers.ps1
```

**What happens:**
- ✅ Opens 5 separate PowerShell windows (one per worker)
- ✅ Each worker starts on ports 6000-6004 (TCP) and 7000-7004 (HTTP)
- ✅ Workers auto-register with broker via UDP
- ✅ Workers connect to NIO broadcast channel

**Each worker window shows:**
```
Worker-1 started. Listening on port 6000
Registered with broker: REGISTERED
Web interface: http://localhost:7000
```

---

### Step 4: Submit a Task and See It Work! 🎉

**Option A: Web Interface (Recommended)**

1. Open browser: **http://localhost:8080/index.html**
2. Fill in the form:
   - **Task ID**: `1001`
   - **Task Name**: `Data Processing Task`
   - **Number of Sub-Tasks**: Select `5 sub-tasks`
   - **Sub-task Data**: Enter data for each (fields appear dynamically)
     - Sub-task 1: `Process batch 1`
     - Sub-task 2: `Process batch 2`
     - Sub-task 3: `Process batch 3`
     - Sub-task 4: `Process batch 4`
     - Sub-task 5: `Process batch 5`
3. Click **"Submit Task via M1 TCP"**
4. ✅ See success message with task ID

**Watch it in action:**
- Open **http://localhost:8080/dashboard.html** to see real-time progress
- Check individual worker dashboards: http://localhost:7000 through 7004

**Option B: Command Line**

```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "My test task"
```

---

### 🎯 You're Done!
Your distributed task broker system is now running with 5 workers processing tasks in parallel!

---

## 📖 Detailed Running Guide

### Manual Startup (Without Scripts)

#### 1. Start Broker Manually
```powershell
# Using Maven
mvn exec:java -Dexec.mainClass="com.computenet.App"

# OR using JAR
java -jar target/ComputeNet-Project-1.0.jar
```

#### 2. Start Individual Workers Manually
```powershell
# Worker 1 (port 6000)
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6000

# Worker 2 (port 6001)
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6001

# Worker 3 (port 6002)
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6002

# Worker 4 (port 6003)
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6003

# Worker 5 (port 6004)
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6004
```

**Note:** Each worker command should be run in a separate terminal window.

---

### Testing Individual Modules

Each module can be tested independently using provided PowerShell scripts:

```powershell
# Test M1 - TCP Task Submission
.\test-m1-tcp-submission.ps1

# Test M2 - Multi-threading
.\test-m2-multithreading.ps1

# Test M3 - NIO Broadcast
.\test-m3-nio-broadcast.ps1

# Test M4 - Multicast
.\test-m4-multicast.ps1

# Test M5 - UDP Worker Registration
.\test-m5-udp-registration.ps1
```

---

## 🔍 What Each Component Does

### Broker Server Modules

1. **TCP Task Receiver (M1)** - Port 5000
   - Receives task submissions from originators
   - Blocking TCP connection for reliable task delivery
   - Uses ExecutorService for multi-threaded processing

2. **ExecutorService (M2)**
   - Thread pool for handling concurrent task processing
   - 10 worker threads for parallel task execution

3. **NIO Broadcast Handler (M3)** - Port 5002
   - Non-blocking I/O for efficient worker communication
   - Broadcasts task updates to connected workers
   - Single-threaded event-driven architecture

4. **HTTP Data Loader (M4)**
   - Loads data from HTTP endpoints
   - Supports GET and POST requests
   - Used for external data integration

5. **UDP Worker Listener (M5)** - Port 5001
   - Lightweight UDP protocol for worker registration
   - Handles heartbeat messages
   - Fast worker discovery and status updates

### Web Interface

- **Port 8080** - Web UI and WebSocket server
- Real-time dashboard for monitoring tasks and workers
- Task submission interface for originators
- WebSocket endpoint (`ws://localhost:8080/ws`) for live updates

## 🛠️ Technologies Used

- **Java 17**
- **Javalin 6.1.3** - Web framework and WebSocket support
- **Jetty 11.0.20** - Embedded web server
- **Gson 2.10.1** - JSON processing
- **SLF4J 2.0.9** - Logging framework
- **JUnit 5.10.0** - Testing framework
- **Maven** - Build tool and dependency management

## 📦 Building the Project

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Compile the Project

```bash
mvn clean compile
```

### Package as JAR

```bash
mvn clean package
```

This creates a fat JAR with all dependencies in `target/ComputeNet-Project-1.0.jar`

## ▶️ Running the System

### 1. Start the Broker Server

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.computenet.App"

# OR using the JAR
java -jar target/ComputeNet-Project-1.0.jar
```

The broker will start on:
- TCP Task Receiver: `localhost:5000`
- UDP Worker Listener: `localhost:5001`
- NIO Broadcast Handler: `localhost:5002`
- Web UI: `http://localhost:8080`
- WebSocket: `ws://localhost:8080/ws`

### 2. Start Worker Clients

```bash
# Worker 1
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6000

# Worker 2
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6001

# Add more workers as needed...
```

### 3. Submit Tasks

**Option A: Web Interface**
1. Open `http://localhost:8080/index.html` in your browser
2. Fill in the form:
   - **Task ID**: Enter number (e.g., `1001`)
   - **Task Name**: Enter descriptive name (e.g., `Data Processing`)
   - **Number of Sub-Tasks**: Select from dropdown (1 to number of registered workers)
   - **Sub-task Data Fields**: Enter data for each sub-task (fields appear dynamically)
3. Click **"Submit Task via M1 TCP"**

**Option B: Command Line**
```bash
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Process this data"
```

**Option C: Interactive Mode**
```bash
java -cp target/ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost
```

## 🌐 Web UI Access

- **Task Submission**: `http://localhost:8080/index.html`
- **Dashboard**: `http://localhost:8080/dashboard.html`
- **API Endpoint**: `http://localhost:8080/api/workers`

## 📡 Communication Protocols

### Task Submission (TCP - Port 5000)
```
Originator -> Broker: <task_data>
Broker -> Originator: TASK_ACCEPTED:<task_id>
```

### Worker Registration (UDP - Port 5001)
```
Worker -> Broker: REGISTER:<tcp_port>
Broker -> Worker: REGISTERED
```

### Heartbeat (UDP - Port 5001)
```
Worker -> Broker: HEARTBEAT
Broker -> Worker: ACK
```

### Task Results (NIO - Port 5002)
```
Worker -> Broker: RESULT:<task_id>:<subtask_id>:<result>
```

### WebSocket Messages (Port 8080)
```json
// Request Status
{"type": "REQUEST_STATUS"}

// Status Response
{
  "type": "STATUS_UPDATE",
  "data": {
    "activeWorkers": 5,
    "pendingTasks": 10,
    "completedTasks": 23
  }
}
```

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## 📝 Architecture

### Task Flow

1. **Originator** submits task via TCP (Port 5000)
2. **TaskTcpReceiver** (M1) receives and passes to ExecutorService (M2)
3. **TaskManager** creates task and splits into sub-tasks
4. **WorkerNIOHandler** (M3) broadcasts task to registered workers
5. **Workers** process sub-tasks and return results
6. **TaskManager** aggregates results
7. **Dashboard** displays real-time progress via WebSocket

### Worker Lifecycle

1. Worker starts and sends UDP registration (Port 5001)
2. **WorkerUdpListener** (M5) registers worker in TaskManager
3. Worker connects to NIO channel (Port 5002) for broadcasts
4. Worker sends periodic heartbeats via UDP
5. Worker processes tasks and sends results via NIO

## 🔧 Configuration

All ports are configurable in the respective class files:

- `TaskTcpReceiver.java`: TCP port (default: 5000)
- `WorkerUdpListener.java`: UDP port (default: 5001)
- `WorkerNIOHandler.java`: NIO port (default: 5002)
- `BrokerServer.java`: Web UI port (default: 8080)

## 📊 Monitoring

Access the dashboard at `http://localhost:8080/dashboard.html` to monitor:
- Active workers count
- Pending tasks
- Completed tasks
- Real-time task queue
- Worker status

## 🐛 Troubleshooting

### Common Issues and Solutions

#### 1. Port Already in Use Error

**Symptoms:**
```
ERROR: The following ports are already in use:
  - Port 5000
  - Port 8080
```

**Solution:**
```powershell
# Run the cleanup script
.\cleanup-ports.ps1

# This will kill all processes using ports 5000-5004, 6000-6004, 7000-7004, 8080
```

**Manual Fix (Windows):**
```powershell
# Find process using port
netstat -ano | findstr :5000

# Kill the process (replace <PID> with actual process ID)
taskkill /PID <PID> /F
```

---

#### 2. Workers Not Registering

**Check:**
1. ✅ Broker is running (check port 5001 UDP)
2. ✅ No firewall blocking UDP traffic
3. ✅ Worker shows "Registered with broker: REGISTERED" message

**Fix:**
```powershell
# Restart workers
.\cleanup-ports.ps1
.\start-workers.ps1
```

---

#### 3. Maven Build Fails

**Solution:**
```powershell
# Force update dependencies
mvn clean install -U

# Clear Maven cache if needed (from user home directory)
mvn clean install
```

---

#### 4. "Class Not Found" Error

**Symptoms:**
```
Error: Could not find or load main class com.computenet.App
```

**Solution:**
```powershell
# Rebuild project
mvn clean package -DskipTests

# Verify JAR was created
ls target/ComputeNet-Project-1.0.jar
```

---

#### 5. Dashboard Not Updating

**Check:**
1. Open browser console (F12)
2. Look for WebSocket errors
3. Verify broker is running

**Fix:**
```powershell
# Restart broker
# Stop current broker (Ctrl+C)
.\start-broker.ps1

# Refresh dashboard page in browser
```

---

## 📊 System Configuration

### Port Assignments

| Component | Protocol | Port | Purpose |
|-----------|----------|------|---------|
| TCP Task Receiver (M1) | TCP | 5000 | Task submission from originators |
| UDP Worker Listener (M5) | UDP | 5001 | Worker registration and heartbeat |
| NIO Broadcast Handler (M3) | TCP/NIO | 5002 | Non-blocking worker communication |
| Web UI Server | HTTP/WS | 8080 | Web interface and WebSocket |
| Multicast Group (M4) | Multicast | 230.0.0.1:6005 | Task config broadcasting |
| Worker 1 TCP | TCP | 6000 | Sub-task processing |
| Worker 1 HTTP | HTTP | 7000 | Worker web dashboard |
| Worker 2 TCP | TCP | 6001 | Sub-task processing |
| Worker 2 HTTP | HTTP | 7001 | Worker web dashboard |
| Worker 3 TCP | TCP | 6002 | Sub-task processing |
| Worker 3 HTTP | HTTP | 7002 | Worker web dashboard |
| Worker 4 TCP | TCP | 6003 | Sub-task processing |
| Worker 4 HTTP | HTTP | 7003 | Worker web dashboard |
| Worker 5 TCP | TCP | 6004 | Sub-task processing |
| Worker 5 HTTP | HTTP | 7004 | Worker web dashboard |

---

## 📚 Additional Documentation

The project includes comprehensive testing and reference guides:

- **QUICK_TEST_GUIDE.md** - 5-minute complete system test
- **COMPLETE-TESTING-GUIDE.md** - Comprehensive test scenarios
- **M1-TCP-TESTING-GUIDE.md** - TCP module testing
- **M2-MULTITHREADING-TESTING-GUIDE.md** - Executor service testing
- **M3-NIO-TESTING-GUIDE.md** - NIO broadcast testing
- **M4_TESTING_GUIDE.md** - Multicast testing
- **M5-UDP-TESTING-GUIDE.md** - UDP registration testing
- **MODULE-REFERENCE.md** - Detailed module documentation
- **PROJECT_REPORT.md** - Full project report and analysis
- **PRESENTATION_GUIDE.md** - Presentation materials
- **INTEGRATION_COMPLETE.md** - Integration documentation

---

## 🎓 Educational Value

This project demonstrates:

### 1. Multi-Protocol Networking
- **TCP**: Reliable, connection-oriented communication for critical task submission
- **UDP**: Fast, connectionless protocol for lightweight worker registration
- **Multicast**: Efficient one-to-many task configuration broadcasting
- **NIO**: Non-blocking I/O for scalable worker communication

### 2. Concurrent Programming
- **Thread Pools**: ExecutorService for managed concurrency
- **Lock-Free Structures**: ConcurrentHashMap, AtomicInteger for thread-safe operations
- **Event-Driven Architecture**: NIO Selector for handling multiple connections

### 3. Distributed Systems Concepts
- **Task Decomposition**: Automatic splitting of tasks into sub-tasks
- **Load Distribution**: Dynamic assignment based on available workers
- **Failure Handling**: Graceful degradation and error recovery
- **Real-Time Monitoring**: WebSocket-based live status updates

### 4. Web Technologies
- **REST APIs**: HTTP endpoints for worker information
- **WebSocket**: Bi-directional real-time communication
- **Responsive UI**: Modern HTML5/CSS3/JavaScript interfaces
- **Real-Time Dashboards**: Live data visualization

---

## 🔒 Security Notes

⚠️ **This is a learning/demonstration project:**

- No authentication implemented
- No encryption on network traffic
- Designed for localhost/trusted network only
- **NOT production-ready**

**For production use, consider adding:**
- SSL/TLS encryption for all network traffic
- Authentication tokens/API keys
- Input validation and sanitization
- Rate limiting
- Network segmentation and firewalls
- Logging and audit trails

---

## 📊 Performance Characteristics

- **Concurrent Connections**: Supports 10+ simultaneous originators
- **Worker Capacity**: Tested with 5 workers, scalable to 100+
- **Task Throughput**: ~50 tasks/second with 5 workers
- **Sub-task Distribution**: Automatic load balancing
- **Broadcast Frequency**: 2-second intervals (M3)
- **Heartbeat Interval**: 5 seconds (UDP)
- **Thread Pool Size**: 10 threads (M2 ExecutorService)
- **Memory Footprint**: ~200MB (broker + 5 workers)

---

## 🛠️ Development Tools

### Recommended IDEs
- **IntelliJ IDEA** (recommended)
- **Eclipse**
- **VS Code** with Java extensions

### Useful Commands

```powershell
# Clean build
mvn clean package

# Run without tests
mvn clean package -DskipTests

# Run with debugging
mvn exec:java -Dexec.mainClass="com.computenet.App" -Dexec.args="-Xdebug"

# Check dependencies
mvn dependency:tree

# Generate documentation
mvn javadoc:javadoc
```

---

## 📞 Support and Resources

### Quick Links
- **Project Repository**: Check your repository for latest updates
- **Issue Tracker**: Report bugs or request features
- **Documentation**: See guides in `Distributed Task Broker/` folder

### Getting Help
1. Check the troubleshooting section above
2. Review module-specific testing guides
3. Examine log output for error messages
4. Check browser console for web interface issues

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Check what's using the port (Windows)
netstat -ano | findstr :<port>

# Kill the process
taskkill /PID <process_id> /F
```

### Maven Dependencies Not Resolved
```bash
# Force update dependencies
mvn clean install -U
```

### IDE Not Recognizing Classes
```bash
# Reimport Maven project in your IDE
# Or run:
mvn eclipse:eclipse  # For Eclipse
mvn idea:idea        # For IntelliJ
```

## 👥 Team Members & Module Ownership

| Member | Module | Component | Responsibilities |
|--------|--------|-----------|------------------|
| **Member 1** | M1 | TCP Task Receiver | Task submission protocol, TCP server, connection handling |
| **Member 2** | M2 | Multi-threading Executor | Thread pool management, task decomposition, concurrent processing |
| **Member 3** | M3 | NIO Broadcast Handler | Non-blocking I/O, worker communication, event-driven architecture |
| **Member 4** | M4 | Multicast Broadcaster | IP multicast implementation, task config distribution, HTTP data loader |
| **Member 5** | M5 | UDP Worker Listener | Worker registration, heartbeat handling, UDP protocol |

### Integration & Collaboration
All modules are integrated into a cohesive system through:
- Shared `TaskManager` for centralized state
- Event-driven communication patterns
- Thread-safe data structures
- Standardized message protocols

---

## 📄 License

This project is for educational purposes.

## 🤝 Contributing

Feel free to submit issues and enhancement requests!

---

**Built with ❤️ for Distributed Systems Learning**
