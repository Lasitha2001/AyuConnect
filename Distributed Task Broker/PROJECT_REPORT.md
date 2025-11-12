# Distributed Task Broker System
## Project Report

---

## Project Title
**Distributed Task Broker with Multi-Protocol Communication**

A modular distributed computing system demonstrating five key network programming concepts: TCP-based task submission, multi-threaded task decomposition, non-blocking I/O worker communication, IP multicast broadcasting, and UDP-based worker registration.

---

## Group Members and Individual Contributions

### Member 1: TCP-based Task Submission (M1)
**Contribution:**
- Implemented reliable task submission mechanism using TCP blocking sockets
- Developed custom text-based protocol for task data transmission
- Created thread-per-connection model for handling multiple client connections
- Integrated with TaskManager for centralized task registry
- Message format: `TaskID:xxx | Name:xxx | Data:xxx | SubTasks:n`

**Key Technical Achievements:**
- TCP server socket listening on port 5000
- BufferedReader/PrintWriter for reliable communication
- Acknowledgment protocol: `TASK_ACCEPTED:taskId`
- Seamless integration with M2 for task processing

### Member 2: Multi-threading for Task Decomposition (M2)
**Contribution:**
- Designed and implemented task decomposition system
- Created TaskSubmissionHandler for splitting tasks into sub-tasks
- Developed concurrent task state management using ConcurrentHashMap
- Implemented AtomicInteger for lock-free progress tracking
- Integrated multicast pre-announcement before sub-task dispatch

**Key Technical Achievements:**
- ExecutorService for concurrent connection handling
- Dynamic task splitting based on available workers (M5 registry)
- Thread-safe state management without explicit locks
- Real-time progress tracking for M3 broadcasts

### Member 3: NIO-based Worker Broadcasting (M3)
**Contribution:**
- Implemented non-blocking I/O using Java NIO framework
- Developed Selector-based event-driven architecture
- Created periodic broadcast mechanism (every 2 seconds)
- Managed worker channel registration and lifecycle

**Key Technical Achievements:**
- Single-threaded selector handling multiple worker connections
- ServerSocketChannel configured for non-blocking operation
- ByteBuffer management for efficient data transfer
- Real-time progress broadcasts: "X% (completed/total)"

### Member 4: Multicast for Group Communication (M4)
**Contribution:**
- Implemented IP multicast for efficient one-to-many communication
- Developed TaskConfigMulticaster for pre-announcing task configuration
- Created HTTP-based manual control interface
- Implemented HttpDataLoader for external data integration
- Designed user-controlled multicast subscription system

**Key Technical Achievements:**
- MulticastSocket on address 230.0.0.1:6005
- TTL=1 for local subnet restriction
- Message format: `TASKCONFIG:taskId:taskName:splitCount:taskData:subTask1|subTask2|...`
- Automatic broadcasting by broker when task is processed
- Worker opt-in control via web UI (default: disabled, workers must enable to receive)
- HTTP GET/POST capabilities for data loading

### Member 5: UDP-based Worker Registration (M5)
**Contribution:**
- Implemented lightweight worker discovery using UDP
- Developed stateless registration protocol
- Created ConcurrentHashMap-based worker registry
- Designed worker metadata extraction from packet headers
- Implemented optional heartbeat mechanism

**Key Technical Achievements:**
- DatagramSocket listening on port 5001
- Registration protocol: `REGISTER:TCP_PORT`
- IP address extraction from DatagramPacket metadata
- Thread-safe worker pool: `Map<String, WorkerDetails>`
- Integration with M2 for dynamic task splitting

---

## System Overview

### Architecture
The Distributed Task Broker is a modular distributed computing system that accepts computational tasks from clients, decomposes them into sub-tasks, and distributes them across multiple worker nodes for parallel execution.

### Component Interaction
```
┌─────────────────────────────────────────────────────────────┐
│                    BROKER SERVER                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  M5 (UDP:5001)          M1 (TCP:5000)                      │
│  Worker Registry   ←─→  Task Receiver                      │
│       ↓                      ↓                              │
│  ┌──────────────────────────────────────────┐              │
│  │    TaskManager (Central Registry)        │              │
│  │  - Active tasks: ConcurrentHashMap       │              │
│  │  - Worker pool: ConcurrentHashMap        │              │
│  │  - Progress tracking: AtomicInteger      │              │
│  └──────────────┬───────────────────────────┘              │
│                 ↓                                           │
│  M2 (Task Decomposition & Distribution)                    │
│       │                                                     │
│       ├─→ M4 (Multicast:6005): Pre-broadcast config        │
│       │                                                     │
│       └─→ Direct TCP dispatch to workers                   │
│                                                             │
│  M3 (NIO:5002)                                             │
│  Progress Broadcast (every 2s)                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                         │
                         ↓
          ┌──────────────────────────┐
          │   WORKER CLIENTS (5)     │
          ├──────────────────────────┤
          │  TCP Ports: 6000-6004    │
          │  HTTP Ports: 7000-7004   │
          │  - M5 registration       │
          │  - M3 NIO connection     │
          │  - M4 multicast listener │
          │  - M2 sub-task execution │
          └──────────────────────────┘
```

### Port Allocation
- **5000**: M1 TCP task submission
- **5001**: M5 UDP worker registration
- **5002**: M3 NIO worker connections
- **6000-6004**: Worker TCP ports (5 workers)
- **6005**: M4 Multicast group
- **7000-7004**: Worker HTTP API ports (5 workers)
- **8080**: Broker HTTP/REST API

### Workflow
1. **Worker Registration (M5)**: Workers send `REGISTER:TCP_PORT` via UDP to port 5001
2. **Task Submission (M1)**: Client submits task via TCP to port 5000
3. **Task Creation**: TaskManager creates task with unique ID (starting from 1000)
4. **Task Decomposition (M2)**: TaskSubmissionHandler splits task into N sub-tasks (N = number of workers)
5. **Multicast Pre-announcement (M4)**: Broker automatically broadcasts task config to multicast group (workers must opt-in to receive)
6. **Sub-task Dispatch (M2)**: Sends individual sub-tasks to workers via TCP
7. **Progress Broadcasting (M3)**: NIO handler broadcasts progress every 2 seconds
8. **Result Collection (M2)**: TaskManager collects results from workers

---

## Network Programming Concepts Used

### 1. TCP Socket Programming (M1)
**Concept**: Connection-oriented, reliable communication protocol

**Implementation Details:**
- **ServerSocket**: Listens for incoming connections on port 5000
- **Socket**: Represents client connection
- **Blocking I/O**: Thread blocks until data available
- **Three-way Handshake**: Ensures connection establishment
- **Ordered Delivery**: Packets arrive in sequence
- **Error Detection**: Checksums and acknowledgments

**Benefits:**
- Guaranteed delivery of tasks
- No data loss during transmission
- Suitable for critical task submission

### 2. Multi-threading (M2)
**Concept**: Concurrent execution of multiple threads

**Implementation Details:**
- **ExecutorService**: Manages thread pool for client connections
- **ConcurrentHashMap**: Thread-safe map for task state
- **AtomicInteger**: Lock-free atomic operations for counters
- **Task Decomposition**: Parallel sub-task distribution

**Benefits:**
- Multiple clients handled simultaneously
- Lock-free synchronization reduces contention
- Efficient resource utilization

### 3. Java NIO - Non-blocking I/O (M3)
**Concept**: Event-driven I/O with single-thread multiplexing

**Implementation Details:**
- **Selector**: Monitors multiple channels for I/O events
- **ServerSocketChannel**: Non-blocking server socket
- **SocketChannel**: Non-blocking client connections
- **ByteBuffer**: Efficient data buffer management
- **SelectionKey**: Represents channel registration with selector

**Benefits:**
- One thread handles hundreds of connections
- No blocking on I/O operations
- Scalable architecture
- Efficient progress broadcasting

### 4. IP Multicast (M4)
**Concept**: One-to-many network layer communication

**Implementation Details:**
- **MulticastSocket**: UDP-based group socket
- **Multicast Group**: 230.0.0.1 (Class D address)
- **TTL (Time-to-Live)**: Set to 1 for local subnet
- **IGMP**: Internet Group Management Protocol for group membership
- **Manual Control**: HTTP API for enabling/disabling

**Benefits:**
- Single packet reaches all subscribed workers
- Network bandwidth: O(1) regardless of worker count
- Efficient for configuration distribution
- Optional feature (disabled by default)

### 5. UDP - User Datagram Protocol (M5)
**Concept**: Connectionless, lightweight communication

**Implementation Details:**
- **DatagramSocket**: Sends/receives datagrams
- **DatagramPacket**: Encapsulates data and destination
- **Stateless Protocol**: No connection establishment
- **IP Extraction**: Uses packet metadata for sender identification

**Benefits:**
- Minimal overhead (no handshake)
- Suitable for non-critical registration
- Fast worker discovery
- Simple request-response pattern

### 6. Concurrent Programming
**Concepts Applied:**
- **ConcurrentHashMap**: Lock-free hash table
- **AtomicInteger**: Atomic operations without locks
- **Thread-safe Collections**: Safe concurrent access
- **Synchronization**: Proper resource sharing

### 7. Network Protocol Design
**Custom Protocols:**
- **M1 Protocol**: `TaskID:xxx | Name:xxx | Data:xxx | SubTasks:n`
- **M5 Protocol**: `REGISTER:TCP_PORT`
- **M4 Protocol**: `TASKCONFIG:taskId:taskName:splitCount:taskData:subTasks`
- **M3 Protocol**: Progress percentage strings

**Design Principles:**
- Text-based for debugging
- Delimiter-based parsing
- Self-contained messages
- Backward compatibility

---

## Screenshots of Outputs

### 1. System Startup
**Broker Server Initialization:**
```
========================================
DISTRIBUTED TASK BROKER - Starting
========================================

Initializing modules...
- M1: TCP Task Receiver
- M2: Task Manager & Decomposition Engine
- M3: NIO Worker Handler
- M4: Multicast Task Config Broadcaster
- M5: UDP Worker Registration Listener

Broker starting with configuration:
- TCP Port (M1): 5000
- UDP Port (M5): 5001
- NIO Port (M3): 5002
- HTTP Port: 8080
- Multicast Address (M4): 230.0.0.1:6005

========================================
All modules started successfully!
Broker is ready to accept tasks.
========================================

M1: TCP Task Receiver listening on port 5000
M5: UDP Listener started on port 5001
M3: NIO Handler listening on port 5002
M3: Non-blocking broadcast enabled (every 2 seconds)
M4: Multicast initialized on 230.0.0.1:6005
HTTP server started on port 8080
```

**Worker Client Startup (Worker 1):**
```
========================================
Worker Client Starting
========================================
Worker ID: Worker-1
TCP Port: 6000
HTTP Port: 7000
Multicast: DISABLED (default)
========================================

M5: Registering with broker via UDP (port 5001)...
M5: Sent registration: REGISTER:6000
M5: Registration confirmed: REGISTERED

M3: Connecting to NIO broadcast channel (port 5002)...
M3: Connected to NIO channel

HTTP API started on port 7000
- GET /status - Worker status
- POST /multicast/toggle - Enable/disable M4

========================================
Worker-1 Ready!
Waiting for sub-tasks from broker...
========================================
```

### 2. M1 - TCP Task Submission
**Client Terminal:**
```
========================================
Originator Client - Submitting Task
========================================

Connecting to broker: localhost:5000
Connected successfully!

Sending task:
TaskID:1001 | Name:CustomerDataProcessing | Data:Process 10000 customer records | SubTasks:5

Waiting for acknowledgment...
Received: TASK_ACCEPTED:1001

Task submitted successfully!
Task ID: 1001
========================================
```

**Broker Terminal:**
```
M1: Originator connected. Handing off to Executor...
M1: Received task data: TaskID:1001 | Name:CustomerDataProcessing | Data:Process 10000 customer records | SubTasks:5
TaskManager: New Task 'CustomerDataProcessing' created with ID: 1001. Split into 5 sub-tasks.
M1: Task 'CustomerDataProcessing' accepted with ID: 1001
M1: Send acknowledgment back to client
```

### 3. M2 - Task Decomposition and Distribution
**Broker Terminal:**
```
M2: Processing task 1001 - CustomerDataProcessing
M2: Available workers: 5
M2: Splitting task into 5 sub-tasks

TaskManager: Assigned sub-task 1001-1 to worker 192.168.1.5:6000
TaskManager: Assigned sub-task 1001-2 to worker 192.168.1.5:6001
TaskManager: Assigned sub-task 1001-3 to worker 192.168.1.5:6002
TaskManager: Assigned sub-task 1001-4 to worker 192.168.1.5:6003
TaskManager: Assigned sub-task 1001-5 to worker 192.168.1.5:6004

M2: Dispatching sub-tasks to workers via TCP...
M2: Sub-task 1001-1 dispatched to 192.168.1.5:6000
M2: Sub-task 1001-2 dispatched to 192.168.1.5:6001
M2: Sub-task 1001-3 dispatched to 192.168.1.5:6002
M2: Sub-task 1001-4 dispatched to 192.168.1.5:6003
M2: Sub-task 1001-5 dispatched to 192.168.1.5:6004

M2: All sub-tasks dispatched successfully
```

**Worker Terminal (Worker 1):**
```
Worker-1: Received sub-task 1001-1
Worker-1: Processing sub-task data: Process customer records 1-2000
Worker-1: Sub-task 1001-1 completed
Worker-1: Sending result back to broker
```

### 4. M3 - NIO Progress Broadcasting
**Broker Terminal:**
```
M3: Selector detected 5 ready keys
M3: Worker connected via NIO: /192.168.1.5:50234
M3: Worker connected via NIO: /192.168.1.5:50235
M3: Worker connected via NIO: /192.168.1.5:50236
M3: Worker connected via NIO: /192.168.1.5:50237
M3: Worker connected via NIO: /192.168.1.5:50238
TaskManager: NIO channel registered for broadcast.

[After 2 seconds]
M3: Broadcasting progress to 5 workers
M3: Task 1001 progress: 20% (1/5)

[After 4 seconds]
M3: Broadcasting progress to 5 workers
M3: Task 1001 progress: 40% (2/5)

[After 6 seconds]
M3: Broadcasting progress to 5 workers
M3: Task 1001 progress: 60% (3/5)

[After 8 seconds]
M3: Broadcasting progress to 5 workers
M3: Task 1001 progress: 80% (4/5)

[After 10 seconds]
M3: Broadcasting progress to 5 workers
M3: Task 1001 progress: 100% (5/5)
```

**Worker Terminal (Worker 2):**
```
M3: Received progress update: 20% (1/5)
M3: Received progress update: 40% (2/5)
M3: Received progress update: 60% (3/5)
M3: Received progress update: 80% (4/5)
M3: Received progress update: 100% (5/5)
```

### 5. M4 - Multicast Task Configuration
**Broker Terminal (automatic broadcast during task processing):**
```
M2: Processing task 1002
M4: Broadcasted config for task 1002 'ImageProcessing' (splits: 5)
M4: Task configuration broadcasted successfully (with 5 sub-tasks)
M4: Wait complete - workers ready
TASKCONFIG:1002:ImageProcessing:5:Resize and compress images:SubTask-1/5:...|SubTask-2/5:...|...
```

**Enabling Multicast on Worker (via Web UI or HTTP):**
```
POST http://localhost:7000/api/multicast/toggle
Body: { "enabled": true }

Response:
{
  "success": true,
  "message": "M4 multicast enabled",
  "enabled": true
}
```

**Worker Terminal (after enabling multicast):**
```
M4: Multicast listener ENABLED by user (localStorage)
M4: Worker joined multicast group 230.0.0.1:6005
M4: Multicast listener started
```

**Worker Terminal (receiving multicast - only if enabled):**
```
M4: Received multicast message
M4: TASKCONFIG:1002:ImageProcessing:5:Resize and compress images:SubTask-1/5:...|SubTask-2/5:...|...
M4: Task 1002 will be split into 5 sub-tasks
M4: Pre-allocating resources for sub-task execution
```

**Note:** Broker automatically broadcasts when task is processed. Workers must opt-in via web UI to receive multicast messages.

### 6. M5 - UDP Worker Registration
**Worker Terminal:**
```
M5: Sending registration to broker (localhost:5001)
M5: Message: REGISTER:6000
M5: Waiting for acknowledgment...
M5: Received: REGISTERED
M5: Successfully registered with broker
```

**Broker Terminal:**
```
M5: UDP Listener started on port 5001
M5: Received UDP message: REGISTER:6000 from 192.168.1.5:54321
TaskManager: Worker registered in memory: 192.168.1.5:6000

M5: Received UDP message: REGISTER:6001 from 192.168.1.5:54322
TaskManager: Worker registered in memory: 192.168.1.5:6001

M5: Received UDP message: REGISTER:6002 from 192.168.1.5:54323
TaskManager: Worker registered in memory: 192.168.1.5:6002

M5: Received UDP message: REGISTER:6003 from 192.168.1.5:54324
TaskManager: Worker registered in memory: 192.168.1.5:6003

M5: Received UDP message: REGISTER:6004 from 192.168.1.5:54325
TaskManager: Worker registered in memory: 192.168.1.5:6004

M5: Total registered workers: 5
```

### 7. Complete Task Execution Flow
**Integrated Output:**
```
========================================
COMPLETE TASK EXECUTION - Task 1003
========================================

[M5] 5 workers registered: 192.168.1.5:6000-6004

[M1] Client connected: Submitting task 'DataAnalysis'
[M1] Task 1003 accepted

[M2] Task decomposition started
[M2] Splitting into 5 sub-tasks
[M4] Broadcasting task config via multicast
[M2] Dispatching sub-tasks to workers

[M3 - t=2s] Progress: 20% (1/5)
[M3 - t=4s] Progress: 40% (2/5)
[M3 - t=6s] Progress: 60% (3/5)
[M3 - t=8s] Progress: 80% (4/5)
[M3 - t=10s] Progress: 100% (5/5)

[M2] All sub-tasks completed
[M2] Task 1003 finished successfully

========================================
Task Execution Summary:
- Task ID: 1003
- Sub-tasks: 5
- Workers used: 5
- Total time: 10.2 seconds
- Status: COMPLETED
========================================
```

### 8. HTTP API Status Check
**Request:**
```
GET http://localhost:7000/status
```

**Response:**
```json
{
  "workerId": "Worker-1",
  "tcpPort": 6000,
  "httpPort": 7000,
  "status": "IDLE",
  "m3NioConnected": true,
  "m4MulticastEnabled": false,
  "m5Registered": true,
  "subTasksCompleted": 0,
  "uptime": "00:15:32"
}
```

---

## Challenges Faced and Solutions

### Challenge 1: Port Conflicts During Testing
**Problem:**
- Multiple test runs left Java processes holding ports (5000-8080)
- Workers failed to start with "Port already in use: bind" error
- Manual process cleanup was time-consuming and error-prone

**Solution:**
- Created `cleanup-ports.ps1` PowerShell script
- Automated port scanning using `Get-NetTCPConnection`
- Kills processes using ports 5000-8080 automatically
- Integrated port availability checks in `start-broker.ps1` and `start-workers.ps1`
- Added pre-flight validation before starting services

**Outcome:**
- Zero manual port cleanup required
- Clean startup every time
- Graceful error messages with solution suggestions

### Challenge 2: NIO Selector Thread Management
**Problem:**
- Selector.select() blocks indefinitely if no events occur
- Progress broadcasts needed to happen every 2 seconds
- Balancing between event responsiveness and broadcast timing

**Solution:**
- Used `selector.select(100)` with 100ms timeout
- Check elapsed time after each select() call
- Trigger broadcast when 2 seconds elapsed
- Maintains responsiveness to channel events while ensuring periodic broadcasts

**Outcome:**
- Reliable 2-second broadcast intervals
- Responsive to worker connections/disconnections
- Single-threaded efficiency maintained

### Challenge 3: Multicast Network Configuration
**Problem:**
- Multicast not working on some network configurations
- Windows Firewall blocking multicast packets
- Multi-homed systems joining wrong network interface
- Difficulty debugging multicast group membership
- Need to ensure workers can opt-out if multicast unavailable

**Solution:**
- Broker automatically broadcasts when task is processed (always enabled on broker side)
- Workers opt-in via web UI (default: disabled, m4MulticastEnabled = false)
- Created HTTP API for workers to enable/disable multicast subscription
- Set TTL=1 to restrict to local subnet
- Provided clear error messages for network issues
- Workers without multicast enabled simply don't receive config (no error)

**Outcome:**
- System works on all networks (broker always broadcasts, workers can opt-out)
- Workers control their own multicast subscription
- Easy troubleshooting - workers can disable if multicast unavailable
- No impact on task processing if multicast fails (workers still receive sub-tasks via TCP)

### Challenge 4: Task Decomposition Synchronization
**Problem:**
- Multiple threads updating task state concurrently
- Race conditions in progress tracking
- Need for atomic counter updates
- Avoiding explicit locks for performance

**Solution:**
- Used `ConcurrentHashMap` for thread-safe task registry
- Employed `AtomicInteger` for completion counter
- Lock-free atomic operations: `incrementAndGet()`
- Immutable `TaskState` record pattern

**Outcome:**
- Zero race conditions
- No deadlocks or lock contention
- High-performance concurrent updates
- Clean, readable code without explicit synchronization

### Challenge 5: Worker Registration Timing
**Problem:**
- Task submitted before workers registered
- TaskManager shows 0 workers, splits task into 1 sub-task
- Workers register after task already dispatched

**Solution:**
- Start workers first, then broker (documented in guides)
- Added worker count validation in M2
- Display available worker count on broker startup
- Graceful handling when worker count < requested splits
- Documentation emphasizes proper startup order

**Outcome:**
- Clear startup procedures in all documentation
- Automatic adjustment to available workers
- No hard-coded worker expectations

### Challenge 6: Message Protocol Parsing
**Problem:**
- Initial pipe-only delimiter was ambiguous
- Difficulty parsing optional fields
- Backward compatibility with simple task data

**Solution:**
- Hybrid delimiter system: pipe (`|`) for sections, colon (`:`) for key-value
- Format: `TaskID:xxx | Name:xxx | Data:xxx | SubTasks:n`
- Fallback parsing for simple task strings
- Robust error handling for malformed messages

**Outcome:**
- Clear, human-readable protocol
- Easy debugging with plain text
- Flexible field parsing
- Backward compatible

### Challenge 7: Cross-module Integration Testing
**Problem:**
- Difficult to test individual modules in isolation
- Integration bugs only appeared during full system testing
- Long feedback cycles

**Solution:**
- Created separate test scripts for each module (M1-M5)
- Each script tests one module's functionality
- Clear expected outcomes documented
- `TESTING_GUIDE.md` with cross-module scenarios

**Outcome:**
- Rapid module-specific testing
- Early bug detection
- Clear regression testing
- Easy demonstration of individual contributions

### Challenge 8: Thread-safe Worker Pool Management
**Problem:**
- Workers connecting/disconnecting asynchronously
- M2 needs consistent view of worker pool
- Concurrent registration from M5

**Solution:**
- `ConcurrentHashMap` for worker registry
- `Collections.unmodifiableList()` for safe iteration in M2
- Copy worker list snapshot before task splitting
- No locking required for reads

**Outcome:**
- Safe concurrent access
- No iterator exceptions
- Predictable task splitting behavior

---

## Conclusion

### Project Achievements
The Distributed Task Broker successfully demonstrates five fundamental network programming concepts through a cohesive, modular architecture. Each module (M1-M5) operates independently while integrating seamlessly to create a fully functional distributed computing system.

### Technical Mastery Demonstrated
1. **Socket Programming**: Both TCP (M1) and UDP (M5) implementations showcase understanding of connection-oriented and connectionless protocols
2. **Concurrency**: Multi-threading (M2) and concurrent data structures demonstrate safe parallel execution
3. **Advanced I/O**: Java NIO (M3) proves mastery of event-driven, non-blocking I/O patterns
4. **Network Layer Protocols**: IP multicast (M4) shows understanding beyond application-layer programming
5. **Protocol Design**: Custom message formats demonstrate ability to design efficient, parseable protocols

### Key Strengths
- **Modularity**: Each member's contribution is independently testable and functional
- **Scalability**: NIO and multicast enable handling hundreds of workers efficiently
- **Reliability**: TCP ensures task submission integrity, concurrent data structures prevent race conditions
- **Flexibility**: Optional multicast, dynamic worker pool, configurable task splitting
- **Production-Ready**: Automated port cleanup, comprehensive error handling, graceful degradation

### Real-World Applications
This architecture is applicable to:
- **Distributed Computing**: Data processing, scientific simulations
- **Microservices**: Service discovery (M5), load balancing (M2), health checks (M3)
- **Real-time Systems**: Live progress updates (M3), event broadcasting (M4)
- **Cloud Platforms**: Task scheduling, resource allocation, worker management

### Learning Outcomes
The team gained practical experience in:
- Network protocol selection and trade-offs
- Concurrent programming without deadlocks
- Event-driven architectures
- Network debugging and troubleshooting
- System integration and testing
- Documentation and presentation skills

### Future Enhancements
Potential improvements for production deployment:
1. **Fault Tolerance**: Task reassignment on worker failure, heartbeat timeout enforcement
2. **Load Balancing**: Intelligent sub-task distribution based on worker capacity
3. **Persistence**: Database storage for tasks and results
4. **Security**: SSL/TLS encryption, authentication, authorization
5. **Monitoring**: Metrics collection, dashboard, alerting
6. **Dynamic Scaling**: Auto-detection of new workers, graceful shutdown
7. **Result Aggregation**: Combine sub-task results into final output

### Final Remarks
This project demonstrates that effective distributed systems require not just understanding individual network protocols, but also knowing when to apply each pattern. The combination of TCP's reliability (M1), UDP's efficiency (M5), NIO's scalability (M3), multicast's optimization (M4), and concurrent programming (M2) creates a robust, efficient system greater than the sum of its parts.

The modular design ensures each team member made a distinct, significant contribution while the seamless integration proves strong collaboration and system design skills. The comprehensive documentation, automated tooling, and thorough testing demonstrate professional software engineering practices.

---

## Appendix

### Testing Scripts
- `cleanup-ports.ps1` - Automated port cleanup utility
- `start-broker.ps1` - Broker startup with validation
- `start-workers.ps1` - Multi-worker startup script
- `test-m1-tcp-submission.ps1` - M1 module test
- `test-m2-multithreading.ps1` - M2 module test
- `test-m3-nio-broadcast.ps1` - M3 module test
- `test-m4-multicast.ps1` - M4 module test
- `test-m5-udp-registration.ps1` - M5 module test

### Documentation Files
- `MODULE-REFERENCE.md` - Complete module documentation
- `TESTING_GUIDE.md` - Comprehensive testing guide
- `INTEGRATION_COMPLETE.md` - System integration guide
- `PRESENTATION_GUIDE.md` - Team contribution guide
- `PROJECT_REPORT.md` - This report

### System Requirements
- **Java**: JDK 11 or higher
- **Maven**: 3.6+ for building
- **Operating System**: Windows 10/11, Linux, macOS
- **Network**: Local network with multicast support (optional)
- **Ports**: 5000-8080 available

### Build and Run
```bash
# Build project
mvn clean package -DskipTests

# Clean ports (if needed)
.\cleanup-ports.ps1

# Start broker
.\start-broker.ps1

# Start workers (in separate terminal)
.\start-workers.ps1

# Submit task
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost "Test Task"
```

---

**Report Prepared By:** Distributed Task Broker Team  
**Date:** November 12, 2025  
**Project Duration:** [Insert your project duration]  
**Course:** Network Programming / Distributed Systems  

---
