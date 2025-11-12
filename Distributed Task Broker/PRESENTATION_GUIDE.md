# Project Presentation Guide - Team Contributions (M1-M5)

## Overview
This guide provides a step-by-step explanation of each team member's contribution to the Distributed Task Broker system, focusing on the concepts, architectural patterns, and design decisions used to implement each module.

---

## Module 1 (M1): TCP-based Task Submission
**Owner**: Team Member 1  
**Core Feature**: Reliable task submission using TCP protocol

### Contribution Overview
Team Member 1 established the foundational communication layer for the entire system, implementing a reliable client-server architecture for task submission.

### Step-by-Step Implementation Concepts

#### Step 1: Understanding the Problem
- **Challenge**: Need a reliable way for clients to submit computational tasks to the broker
- **Why TCP**: Chosen for its guaranteed delivery, ordered packet transmission, and error checking
- **Alternative Rejected**: UDP was not suitable here due to lack of reliability guarantees

#### Step 2: Socket Programming Architecture
- **Concept**: TCP Server Socket Pattern
  - Broker listens on a dedicated port (5000)
  - Accepts incoming client connections
  - Maintains persistent connections for task submission
- **Design Pattern**: Connection-oriented communication
  - Three-way handshake ensures connection establishment
  - Full-duplex communication allows bidirectional data flow

#### Step 3: Message Protocol Design
- **Concept**: Custom Text-based Protocol
  - Simple, human-readable format for debugging
  - Structured message format: `TaskID:xxx | Name:xxx | Data:xxx | SubTasks:n`
  - Delimiter-based parsing (pipe symbol `|` separates fields)
  - Colon (`:`) separates field names from values
- **Why Not Binary**: Text protocol chosen for easier debugging and cross-platform compatibility
- **Flexibility**: Optional fields allow backward compatibility (e.g., simple task data without metadata)

#### Step 4: Thread Management
- **Concept**: Thread-per-Connection Model
  - Each client connection spawns a dedicated thread
  - Isolates client handling to prevent blocking
  - Thread lifecycle: create → handle → cleanup
- **Resource Management**: Proper socket closure to prevent resource leaks

#### Step 5: Task Manager Integration
- **Concept**: Central Task Registry Pattern
  - TCP receiver delegates to TaskManager for task creation
  - TaskManager generates unique task IDs (starting from 1000)
  - Validates and adjusts sub-task count based on available workers
  - Uses AtomicInteger for thread-safe ID generation
- **Response Protocol**: Sends acknowledgment back to client: `TASK_ACCEPTED:taskId`
- **Integration**: Hands off to M2's TaskSubmissionHandler for parallel processing

### Key Architectural Decisions
1. **Blocking I/O**: Used traditional blocking sockets for simplicity and reliability
2. **Port Selection**: 5000 chosen as standard non-privileged port
3. **Error Handling**: Connection failures handled gracefully with proper cleanup
4. **Scalability Consideration**: Thread-per-connection limits scalability but ensures simplicity

### Presentation Talking Points
- "M1 provides the **entry point** for all tasks into the system"
- "TCP guarantees that no task is lost during transmission"
- "Thread-per-connection ensures each client gets dedicated attention"
- "This module establishes the **reliability foundation** for the entire system"

---

## Module 2 (M2): Multi-threading for Concurrent Processing
**Owner**: Team Member 2  
**Core Feature**: Parallel task processing using worker threads

### Contribution Overview
Team Member 2 implemented the core task processing engine, enabling the broker to handle multiple tasks simultaneously through multi-threading.

### Step-by-Step Implementation Concepts

#### Step 1: Understanding Concurrency Needs
- **Challenge**: Single-threaded processing would create bottlenecks
- **Solution**: Thread pool pattern for efficient resource utilization
- **Goal**: Maximize CPU utilization while preventing thread explosion

#### Step 2: Thread Pool Design
- **Concept**: Fixed Thread Pool Pattern (Java ExecutorService)
  - M1's TCP receiver uses ExecutorService for connection handling
  - Each client connection handled by executor thread
  - Eliminates overhead of creating/destroying threads repeatedly
- **Design Pattern**: Task Decomposition Pattern
  - TaskSubmissionHandler splits tasks into N sub-tasks
  - N determined by available workers (from M5 registry)
  - Each sub-task dispatched to individual worker via TCP

#### Step 3: Task State Management
- **Concept**: Concurrent Data Structures (ConcurrentHashMap)
  - Thread-safe map stores active tasks: `Map<Integer, TaskState>`
  - TaskState record: taskName, originalData, totalSubTasks, completedSubTasks, results
  - AtomicInteger tracks completion count without explicit locking
- **Worker Assignment Tracking**: 
  - Maps workers to their assigned sub-tasks
  - Used for load balancing and worker statistics
  - ConcurrentHashMap ensures thread-safe concurrent updates

#### Step 4: Sub-Task Dispatch and Execution
- **Concept**: Distributed Work Distribution
  - TaskSubmissionHandler splits task data into N sub-tasks
  - Sends each sub-task to individual worker via TCP socket
  - Workers execute sub-tasks independently
  - Results collected back in TaskManager
- **Progress Tracking**: 
  - AtomicInteger tracks completed sub-tasks atomically
  - Progress percentage calculated: (completed/total) × 100
  - Used by M3 for real-time broadcast updates

#### Step 5: M4 Integration - Multicast Pre-announcement
- **Concept**: Pre-broadcast Configuration
  - Before dispatching sub-tasks, optionally broadcasts configuration via M4 multicast
  - Message format: `TASKCONFIG:taskId:taskName:splitCount:taskData:subTask1|subTask2|...`
  - Workers receive configuration before individual sub-task assignments
- **Optimization**: Workers can prepare/pre-allocate resources based on config

### Key Architectural Decisions
1. **ExecutorService**: Java's built-in thread pool for connection handling
2. **Task Decomposition**: Split tasks based on available worker count (M5 registry)
3. **ConcurrentHashMap**: Lock-free data structures for task state management
4. **AtomicInteger**: Lock-free counters for progress tracking
5. **TCP Dispatch**: Direct TCP connections to workers for sub-task delivery

### Presentation Talking Points
- "M2 enables **parallel task decomposition** - splits tasks into sub-tasks"
- "ExecutorService manages concurrent client connections efficiently"
- "ConcurrentHashMap and AtomicInteger provide **lock-free synchronization**"
- "This module is the **orchestrator** that distributes work across workers"
- "Integrates with M4 multicast for pre-announcing task configuration"

---

## Module 3 (M3): NIO-based Worker Broadcast
**Owner**: Team Member 3  
**Core Feature**: Efficient non-blocking communication with workers

### Contribution Overview
Team Member 3 introduced advanced I/O techniques using Java NIO (New I/O) to enable scalable, non-blocking communication with multiple workers simultaneously.

### Step-by-Step Implementation Concepts

#### Step 1: Limitations of Traditional I/O
- **Problem**: Blocking I/O requires one thread per connection
  - 100 workers = 100 threads (resource intensive)
- **Solution**: Non-blocking I/O with multiplexing
  - 1 thread can handle 100+ connections
- **Paradigm Shift**: From thread-per-connection to event-driven model

#### Step 2: NIO Core Concepts
- **Channel**: Bidirectional data pathway (replaces streams)
  - SocketChannel for network communication
  - Can be configured as blocking or non-blocking
- **Buffer**: Memory region for data staging
  - ByteBuffer for efficient data manipulation
  - Eliminates intermediate byte array allocations
- **Selector**: Multiplexing coordinator
  - Monitors multiple channels simultaneously
  - Alerts when channels are ready for I/O operations

#### Step 3: Event-Driven Architecture
- **Concept**: Reactor Pattern
  - Single thread monitors multiple connections
  - Responds to I/O events (read ready, write ready, connect)
  - Event loop continuously processes ready channels
- **Selection Keys**: 
  - Interest set: operations you want to monitor (READ, WRITE, ACCEPT)
  - Ready set: operations that are currently possible

#### Step 4: Worker Registration
- **Concept**: Connection Pool Management
  - Workers connect to broker on NIO port (5002)
  - Broker registers each worker's SocketChannel with Selector
  - Maintains worker registry (Map of Channel → Worker Info)
- **Non-blocking Accept**: 
  - ServerSocketChannel.accept() doesn't block
  - New connections detected via Selector

#### Step 5: Broadcast Mechanism
- **Concept**: Periodic Progress Broadcasting
  - Every 2 seconds, broadcasts current task progress to all NIO-connected workers
  - Message format: Progress percentage (e.g., "50% (3/6)")
  - Iterate through all registered channels
  - Write progress to each channel's buffer
- **Write Readiness**: 
  - Only write when channel signals WRITE-ready
  - Prevents blocking on slow network connections
- **Real-time Updates**: Workers receive live progress without polling

#### Step 6: Buffer Management
- **Concept**: Direct vs. Heap Buffers
  - Direct buffers for network I/O (zero-copy)
  - Buffer flip/clear operations for reuse
  - Position/limit/capacity management
- **Serialization**: 
  - Convert task objects to byte sequences
  - Custom protocol or JSON for cross-platform compatibility

### Key Architectural Decisions
1. **Single-threaded Selector**: Simplifies synchronization, adequate for I/O-bound operations
2. **Port Separation**: Dedicated NIO port (5002) isolates from TCP module
3. **Broadcast vs. Unicast**: Chose broadcast to demonstrate NIO's efficiency
4. **Buffer Pooling**: Reuse buffers to reduce garbage collection pressure

### Presentation Talking Points
- "M3 demonstrates **scalability** - one thread handles hundreds of workers"
- "NIO's Selector is like a traffic controller monitoring multiple roads simultaneously"
- "Non-blocking I/O enables **reactive** rather than proactive programming"
- "This module showcases Java's advanced I/O capabilities for high-performance systems"

---

## Module 4 (M4): Multicast for Group Communication
**Owner**: Team Member 4  
**Core Feature**: Efficient one-to-many communication using IP multicast

### Contribution Overview
Team Member 4 implemented network-layer multicast to enable efficient group communication, reducing network bandwidth and broker load when distributing updates to multiple workers.

### Step-by-Step Implementation Concepts

#### Step 1: Understanding Multicast Need
- **Problem**: Broadcasting to N workers requires N individual messages
  - Network bandwidth: O(N)
  - Broker processing: O(N)
- **Solution**: Multicast - single message reaches all subscribers
  - Network bandwidth: O(1)
  - Broker processing: O(1)
- **Use Case**: Task completion notifications, system announcements

#### Step 2: IP Multicast Fundamentals
- **Concept**: Group Communication Primitive
  - Multicast group: Logical address (e.g., 230.0.0.1)
  - Class D IP addresses (224.0.0.0 to 239.255.255.255)
  - Subscription-based: workers join/leave groups dynamically
- **Network Layer**: 
  - Operates at Layer 3 (Network Layer)
  - Routers replicate packets to multiple destinations
  - More efficient than application-layer broadcast

#### Step 3: MulticastSocket Configuration
- **Concept**: UDP-based Group Socket
  - Uses UDP as transport (unreliable, connectionless)
  - Socket joins multicast group via network interface
  - Can send and receive on same group address
- **Network Interface Selection**: 
  - Must bind to specific network interface
  - Important for multi-homed systems

#### Step 4: Group Membership Management
- **Concept**: Internet Group Management Protocol (IGMP)
  - Workers send IGMP join to subscribe to group
  - Routers maintain group membership tables
  - Automatic leave when worker disconnects
- **Time-to-Live (TTL)**: 
  - Controls multicast packet propagation distance
  - TTL=1: Local subnet only
  - Prevents multicast flooding

#### Step 5: Automatic Broker Broadcasting
- **Concept**: Automatic Multicast on Task Processing
  - Broker automatically broadcasts task config when M2 processes a task
  - No manual trigger needed - integrated into task workflow
  - Broadcast happens BEFORE sub-task dispatch (pre-announcement)
- **Design Pattern**: Observer Pattern
  - TaskSubmissionHandler automatically calls TaskConfigMulticaster
  - Workers that have opted-in receive the broadcast

#### Step 6: Message Format Design
- **Concept**: Structured Datagram Protocol
  - Format: `TASKCONFIG:taskId:taskName:splitCount:taskData:subTask1|subTask2|...`
  - Colon (`:`) separates main fields
  - Pipe (`|`) separates sub-task data
  - Example: `TASKCONFIG:1001:DataAnalysis:3:raw_data:chunk1|chunk2|chunk3`
- **Serialization**: 
  - Text-based for human-readable messages
  - Compact format to fit within UDP datagram limits (64KB)
  - No JSON overhead - direct string concatenation

#### Step 7: Worker Opt-in Control
- **Concept**: Worker-Side Subscription Control
  - Broker ALWAYS broadcasts automatically when task is processed
  - Workers must OPT-IN to receive multicast messages
  - Default: DISABLED (m4MulticastEnabled = false)
  - Workers have HTTP API to enable/disable multicast subscription
  - User toggles via web UI checkbox (stored in localStorage)
  - When enabled, worker joins multicast group 230.0.0.1:6005
  - When disabled, worker ignores multicast broadcasts (no listener started)

### Key Architectural Decisions
1. **Automatic Broadcasting**: Broker automatically broadcasts when task is processed (no manual trigger)
2. **Worker Opt-in**: Workers must enable multicast subscription via web UI (default: disabled)
3. **Dedicated Port**: 6005 for multicast to avoid conflicts with M1/M3/M5
4. **Local Subnet**: TTL=1 restricts to local network for safety
5. **UDP Choice**: Multicast inherently uses UDP (no TCP multicast exists)
6. **Pre-announcement**: Broadcasts task config BEFORE M2 dispatches sub-tasks (100ms wait for workers to receive)
7. **HTTP DataLoader**: Includes HTTP GET/POST for external data integration

### Presentation Talking Points
- "M4 demonstrates **network-efficient** distribution - one packet reaches all workers"
- "Multicast reduces broker load from O(N) to O(1) for N workers"
- "Broker **automatically broadcasts** when task is processed - no manual trigger needed"
- "Workers **opt-in** via web UI to receive multicast (default: disabled)"
- "Pre-announces task configuration before sub-task dispatch (100ms delay for worker reception)"
- "Trade-off: efficiency vs. reliability - suitable for non-critical configuration"
- "This module shows understanding of **network-layer protocols** beyond application layer"
- "Includes HTTP DataLoader for external data integration (GET/POST)"

---

## Module 5 (M5): UDP-based Worker Registration
**Owner**: Team Member 5  
**Core Feature**: Lightweight worker discovery and heartbeat mechanism

### Contribution Overview
Team Member 5 implemented a connectionless registration system using UDP, enabling workers to register with the broker without the overhead of TCP connections.

### Step-by-Step Implementation Concepts

#### Step 1: Why UDP for Registration
- **Problem**: TCP registration requires connection establishment overhead
  - Three-way handshake for each registration
  - Connection state maintenance
- **Solution**: UDP for lightweight, stateless registration
  - Single packet registration
  - No connection overhead
- **Use Case**: Worker discovery, heartbeat monitoring

#### Step 2: UDP Socket Programming
- **Concept**: Connectionless Datagram Socket
  - DatagramSocket for sending/receiving packets
  - DatagramPacket encapsulates data + destination
  - No connection establishment required
- **Stateless Nature**: 
  - Each packet independent
  - No guaranteed delivery or ordering
  - Sender doesn't know if receiver got the packet

#### Step 3: Registration Protocol Design
- **Concept**: Request-Response Pattern (Stateless)
  - Worker sends: `REGISTER:TCP_PORT` (e.g., `REGISTER:6000`)
  - Broker responds: `REGISTERED` (acknowledgment)
  - Broker extracts worker IP from packet metadata
  - Single round-trip registration
- **Message Structure**: 
  - Compact text format (colon separator)
  - Self-contained (includes worker's TCP port for task dispatch)
  - No session state required

#### Step 4: Worker Registry Management
- **Concept**: In-Memory Worker Directory
  - ConcurrentHashMap: `String key → WorkerDetails(address, tcpPort, status)`
  - Key format: `ipAddress:port` (e.g., `192.168.1.5:6000`)
  - WorkerDetails record: address, tcpPort, status ("IDLE", "BUSY", etc.)
  - Broker maintains active worker list
  - Used by M2 for task distribution, M3/M4 for broadcasts
- **Concurrency**: 
  - Thread-safe ConcurrentHashMap
  - Handles concurrent registrations from multiple workers
  - No explicit locking required

#### Step 5: Heartbeat Mechanism (Optional)
- **Concept**: Keepalive Protocol
  - Workers can send `HEARTBEAT` UDP messages
  - Broker responds with `ACK` acknowledgment
  - Simple liveness check without timeout enforcement in current implementation
- **Design Pattern**: Ping-Pong Pattern
  - Worker pings broker periodically
  - Broker acknowledges to confirm connectivity
  - Can be extended for failure detection

#### Step 6: Integration with Task Distribution
- **Concept**: Worker Discovery Service
  - M2 queries worker registry to determine available workers
  - Task split count = min(requested splits, available workers)
  - Dynamic adjustment based on current worker pool
- **Graceful Handling**: 
  - System adapts to worker count automatically
  - If 3 workers registered but 5 sub-tasks requested → creates only 3 sub-tasks
  - No hard-coded worker expectations

### Key Architectural Decisions
1. **UDP Choice**: Lightweight protocol for non-critical registration
2. **Port Selection**: 5001 for UDP listener (separate from M1 TCP)
3. **IP Extraction**: Uses DatagramPacket metadata instead of requiring IP in message
4. **No Persistence**: Registry is in-memory only (simple, fast recovery)
5. **Stateless Design**: No connection tracking simplifies implementation
6. **ConcurrentHashMap**: Thread-safe without explicit synchronization

### Presentation Talking Points
- "M5 provides **lightweight discovery** - workers register in one UDP packet"
- "UDP's connectionless nature reduces overhead compared to TCP registration"
- "ConcurrentHashMap provides **thread-safe registry** without locks"
- "IP address extracted from packet metadata - no need to send in message"
- "This module demonstrates understanding of **stateless vs. stateful** protocols"
- "Worker registry enables dynamic task splitting in M2"

---

## System Integration: How Modules Work Together

### Data Flow Architecture
```
                    ┌─────────────────────────────────────┐
                    │  M5 (UDP Registry)                  │
                    │  Workers register → Worker Pool     │
                    └─────────────┬───────────────────────┘
                                  │ provides worker list
                                  ↓
Client → M1 (TCP) → TaskManager → M2 (Task Decomposition)
  5000              create task      split into N sub-tasks
                         │                    │
                         │                    ↓
                         │         ┌──────────────────────┐
                         │         │ M4 (Multicast)       │
                         │         │ Pre-broadcast config │
                         │         │ 230.0.0.1:6005       │
                         │         └──────────────────────┘
                         │                    │
                         ↓                    ↓
                    M3 (NIO Broadcast)   M2 (TCP Dispatch)
                    Progress updates     Sub-tasks to workers
                    every 2 seconds     (blocking TCP sockets)
                         │                    │
                         └────→ Workers execute & return results
```

### Module Dependencies
1. **M1 → TaskManager**: TCP receiver creates tasks in central registry
2. **M1 → M2**: Delegates to TaskSubmissionHandler for task decomposition
3. **M5 → M2**: Worker registry determines sub-task split count
4. **M2 → M4**: Pre-broadcasts task config before dispatching sub-tasks
5. **M2 → Workers**: Dispatches individual sub-tasks via TCP
6. **M3 → Workers**: Broadcasts progress updates every 2 seconds via NIO
7. **M5 → M3**: Worker registry used for NIO channel targeting

### Port Allocation Strategy
- **5000**: M1 TCP task submission
- **5001**: M5 UDP worker registration
- **5002**: M3 NIO worker connections
- **6000-6004**: Worker TCP ports (5 workers)
- **6005**: M4 Multicast group
- **7000-7004**: Worker HTTP ports (5 workers)
- **8080**: Broker HTTP/REST API

---

## Presentation Flow Suggestions

### Introduction (2 minutes)
"Our Distributed Task Broker demonstrates five key networking and concurrency concepts through modular design. Each team member implemented a distinct module showcasing different distributed systems patterns."

### M1 Presentation (3 minutes)
1. **Problem**: Need reliable task submission
2. **Solution**: TCP server socket
3. **Key Concept**: Connection-oriented communication guarantees delivery
4. **Demo**: Show TCP task submission

### M2 Presentation (3 minutes)
1. **Problem**: Sequential processing bottleneck
2. **Solution**: Thread pool for concurrency
3. **Key Concept**: Producer-consumer pattern with blocking queue
4. **Demo**: Show multiple tasks processing simultaneously

### M3 Presentation (4 minutes)
1. **Problem**: Thread-per-worker doesn't scale
2. **Solution**: NIO with Selector for multiplexing
3. **Key Concept**: Event-driven I/O handles hundreds of connections with one thread
4. **Demo**: Show broadcast to multiple workers

### M4 Presentation (4 minutes)
1. **Problem**: Unicast broadcast wastes bandwidth
2. **Solution**: IP multicast for one-to-many communication
3. **Key Concept**: Network-layer group communication
4. **Demo**: Manual HTTP trigger of multicast message

### M5 Presentation (3 minutes)
1. **Problem**: Need lightweight worker discovery
2. **Solution**: UDP registration with heartbeat
3. **Key Concept**: Stateless, connectionless communication
4. **Demo**: Worker registration and automatic timeout

### Integration Demonstration (3 minutes)
1. Start broker with all modules
2. Register workers via M5 (UDP)
3. Submit task via M1 (TCP)
4. Show M2 processing (threads)
5. Trigger M3 broadcast (NIO)
6. Trigger M4 multicast (HTTP)

### Conclusion (2 minutes)
"This project demonstrates mastery of:
- Socket programming (TCP/UDP)
- Concurrency (multi-threading)
- Advanced I/O (NIO)
- Network protocols (multicast)
- Distributed systems patterns (producer-consumer, event-driven)

Each module independently functional yet integrates seamlessly into a cohesive distributed system."

---

## Key Concepts Summary by Module

| Module | Primary Concept | Secondary Concepts | Design Pattern |
|--------|----------------|-------------------|----------------|
| M1 | TCP Socket Programming | Blocking I/O, ExecutorService, Request-Response | Client-Server |
| M2 | Task Decomposition | Multi-threading, ConcurrentHashMap, AtomicInteger | Work Distribution |
| M3 | Non-blocking I/O | Selector, Channel, Buffer, Periodic Broadcast | Reactor Pattern |
| M4 | IP Multicast | UDP, Group Communication, Manual Control, HTTP API | Publish-Subscribe |
| M5 | Connectionless Registration | UDP, ConcurrentHashMap, Worker Discovery | Stateless Service |

---

## Comparative Analysis for Q&A

### Why TCP for M1 but UDP for M5?
- **M1**: Tasks are critical data requiring guaranteed delivery
- **M5**: Registration is non-critical, can be retried

### Why Task Decomposition (M2) vs. NIO Broadcast (M3)?
- **M2**: Splits tasks into sub-tasks, dispatches to workers for distributed execution
- **M3**: Broadcasts live progress updates to all workers without blocking
- **Different Purposes**: M2 distributes work, M3 distributes information

### Why Multicast (M4) Not Default?
- Network complexity: requires multicast-enabled routers
- Reliability: UDP multicast is best-effort
- Controlled testing: manual trigger ensures predictable behavior

### Scalability Comparison
- **M1**: Limited by thread count (~100s connections)
- **M2**: Limited by CPU cores (optimal: cores × 1-2 threads)
- **M3**: Highly scalable (1000s connections per thread)
- **M4**: Most efficient (O(1) network usage regardless of workers)
- **M5**: Lightweight (minimal overhead per worker)

---

## Technical Terms Glossary

- **Blocking I/O**: Thread waits until I/O operation completes
- **Non-blocking I/O**: Thread continues executing, checks I/O status later
- **Multiplexing**: Monitoring multiple I/O channels with single thread
- **Selector**: NIO component that monitors multiple channels for events
- **Channel**: Bidirectional data pathway in NIO
- **Buffer**: Memory region for staging data in NIO
- **Datagram**: Self-contained, independent packet (UDP)
- **Multicast Group**: Logical address for group communication
- **TTL**: Time-to-Live, limits multicast packet propagation
- **Heartbeat**: Periodic alive signal from worker to broker
- **Soft-state**: State that expires automatically if not refreshed

---

## Questions to Anticipate

**Q: Why not use only TCP for everything?**
A: Different protocols optimize for different scenarios. TCP guarantees delivery but has overhead. UDP is lightweight for non-critical data. Multicast is most efficient for group communication.

**Q: How does thread pool size get determined?**
A: Typically CPU cores × 2 for mixed I/O and CPU workloads. Configurable based on task characteristics.

**Q: What if multicast isn't supported by network?**
A: Falls back to M3 (NIO broadcast) which works on all networks. M4 is an optimization, not a requirement.

**Q: How do you prevent duplicate sub-task processing?**
A: TaskManager assigns unique sub-task IDs. Each worker receives exactly one sub-task assignment via TCP.

**Q: What happens if worker crashes?**
A: Worker remains in M5 registry but won't respond to sub-task assignments. M2 can detect failed dispatch. Future enhancement: timeout-based cleanup.

**Q: Can the system handle thousands of workers?**
A: M3 (NIO) and M4 (multicast) scale well. M1 uses ExecutorService which handles hundreds of concurrent clients. M5 registry is ConcurrentHashMap with O(1) lookups.

---

## Presentation Tips

### For M1 (TCP)
- Emphasize **reliability** - packets arrive in order, guaranteed
- Show three-way handshake diagram
- Mention real-world use: HTTP, FTP, SSH

### For M2 (Threading)
- Emphasize **parallelism** - multiple tasks simultaneously
- Show thread timeline diagram
- Mention real-world use: Web servers, database engines

### For M3 (NIO)
- Emphasize **scalability** - one thread, many connections
- Show Selector monitoring multiple channels diagram
- Mention real-world use: Netty, Node.js event loop

### For M4 (Multicast)
- Emphasize **efficiency** - one packet, multiple receivers
- Show network topology with multicast replication
- Mention real-world use: Video streaming, stock tickers

### For M5 (UDP)
- Emphasize **lightweight** - no connection overhead
- Show stateless request-response diagram
- Mention real-world use: DNS, DHCP, gaming

---

## Final Checklist for Presentation

- [ ] Each team member can explain their module's core concept
- [ ] Diagrams prepared for each module's architecture
- [ ] Demo environment tested and ready
- [ ] Port allocation memorized (5000, 5001, 5002, 6005, 8080)
- [ ] Design pattern names known (Reactor, Producer-Consumer, etc.)
- [ ] Scalability comparisons understood
- [ ] Integration flow explained clearly
- [ ] Q&A answers rehearsed
- [ ] Backup slides prepared for deep technical questions
- [ ] Code snippets ready (if allowed in presentation)

