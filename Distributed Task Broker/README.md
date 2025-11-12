# Distributed Task Broker System

A distributed task brokering system built with Java that supports TCP, NIO, and UDP communication protocols for task distribution and worker management.

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

## 🚀 Features

### Broker Server Components

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

## 👥 Team Members

- **Member 1**: TCP Task Receiver (TaskTcpReceiver.java)
- **Member 2**: Executor Service (Thread pool management)
- **Member 3**: NIO Handler (WorkerNIOHandler.java)
- **Member 4**: HTTP Data Loader (HttpDataLoader.java)
- **Member 5**: UDP Listener (WorkerUdpListener.java)

## 📄 License

This project is for educational purposes.

## 🤝 Contributing

Feel free to submit issues and enhancement requests!

---

**Built with ❤️ for Distributed Systems Learning**
