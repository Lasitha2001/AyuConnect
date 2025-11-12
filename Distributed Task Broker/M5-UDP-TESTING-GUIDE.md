# M5 UDP Worker Registration - Testing Guide

## ✅ Implementation Status

The M5 UDP Worker Registration has been successfully implemented with:

### Components Implemented:

1. **WorkerUdpListener.java** (Broker Side - Port 5001)
   - **Location**: `src/main/java/com/computenet/broker/server/WorkerUdpListener.java`
   - ✅ Listens on UDP port 5001 using DatagramSocket
   - ✅ Receives "REGISTER:PORT" messages from workers
   - ✅ Sends "REGISTERED" acknowledgment back
   - ✅ Handles "HEARTBEAT" messages
   - ✅ Integrates with TaskManager

2. **WorkerClient.java** (Worker Side)
   - **Location**: `src/main/java/com/computenet/client/WorkerClient.java`
   - ✅ Sends UDP registration packet on startup (format: "REGISTER:<workerTcpPort>")
   - ✅ Waits for "REGISTERED" acknowledgment with 5-second timeout
   - ✅ Sends periodic heartbeats every 10 seconds
   - ✅ Supports command-line arguments for broker host and worker port

---

## 🚀 Quick Test Methods

### Method 1: Automated Test Script (RECOMMENDED)

**PowerShell:**
```powershell
.\test-m5-udp-registration.ps1
```

This will:
- ✅ Check if JAR exists (builds if needed)
- ✅ Start the Broker Server
- ✅ Start 5 Worker Clients with different ports (6000-6004)
- ✅ Show all registration messages in separate windows

**Note:** The script uses `mvn exec:java` for execution. If you encounter Maven errors, use Method 2 (Manual JAR-based) instead.

### Method 3: Manual Testing Using JAR (Fastest)

**Step 1: Start Broker Server (Terminal 1)**
```powershell
java -jar target\ComputeNet-Project-1.0.jar
```

**Wait for this output:**
```
M5: UDP Listener started on port 5001
```

**Step 2: Start 5 Workers (Terminals 2-6)**

Terminal 2 - Worker 1:
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6000
```

Terminal 3 - Worker 2:
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6001
```

Terminal 4 - Worker 3:
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6002
```

Terminal 5 - Worker 4:
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6003
```

Terminal 6 - Worker 5:
```powershell
java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6004
```

### Method 4: Manual Testing Using Maven

**Step 1: Start Broker (Terminal 1)**
```powershell
mvn exec:java -Dexec.mainClass="com.computenet.App"
```

**Step 2: Start Workers (Terminals 2-6)**
```powershell
# Worker 1
mvn exec:java -Dexec.mainClass="com.computenet.client.WorkerClient" -Dexec.args="localhost 6000"

# Worker 2
mvn exec:java -Dexec.mainClass="com.computenet.client.WorkerClient" -Dexec.args="localhost 6001"

# Worker 3
mvn exec:java -Dexec.mainClass="com.computenet.client.WorkerClient" -Dexec.args="localhost 6002"

# Worker 4
mvn exec:java -Dexec.mainClass="com.computenet.client.WorkerClient" -Dexec.args="localhost 6003"

# Worker 5
mvn exec:java -Dexec.mainClass="com.computenet.client.WorkerClient" -Dexec.args="localhost 6004"
```

---

## ✅ Expected Output - Success Criteria

### On Broker Server Terminal:
```
Starting Distributed Task Broker...
=====================================
M1: TCP Task Receiver listening on port 5000
M3: NIO Handler listening on port 5002
M5: UDP Listener started on port 5001
Broker Server started successfully!
  TCP Task Receiver: port 5000
  UDP Worker Listener: port 5001
  NIO Broadcast Handler: port 5002
  Web UI: http://localhost:8080
  WebSocket: ws://localhost:8080/ws

M5: Received UDP message: REGISTER:6000 from 127.0.0.1:xxxxx
TaskManager: Worker registered in memory: 127.0.0.1:6000

M5: Received UDP message: REGISTER:6001 from 127.0.0.1:xxxxx
TaskManager: Worker registered in memory: 127.0.0.1:6001

M5: Received UDP message: REGISTER:6002 from 127.0.0.1:xxxxx
TaskManager: Worker registered in memory: 127.0.0.1:6002

M5: Received UDP message: REGISTER:6003 from 127.0.0.1:xxxxx
TaskManager: Worker registered in memory: 127.0.0.1:6003

M5: Received UDP message: REGISTER:6004 from 127.0.0.1:xxxxx
TaskManager: Worker registered in memory: 127.0.0.1:6004
```

## Expected Results

### On Each Worker Terminal:
```
Worker Client starting...
Broker Host: localhost
Worker Port: 6000
Worker: Sent registration to broker
Worker: Registration acknowledged: REGISTERED
Worker: Connected to NIO broadcast channel
```

---

## 🎯 Verification Methods

### Method 1: Count Registration Messages in Broker Terminal
Look for **5 instances** of:
```
M5: Received UDP message: REGISTER:
TaskManager: Worker registered in memory:
```

### Method 2: Check via Web API
Open browser: `http://localhost:8080/api/workers`

**Expected JSON Response (5 workers):**
```json
[
  {"address":"127.0.0.1","tcpPort":6000,"status":"IDLE"},
  {"address":"127.0.0.1","tcpPort":6001,"status":"IDLE"},
  {"address":"127.0.0.1","tcpPort":6002,"status":"IDLE"},
  {"address":"127.0.0.1","tcpPort":6003,"status":"IDLE"},
  {"address":"127.0.0.1","tcpPort":6004,"status":"IDLE"}
]
```

### Method 3: Check Dashboard UI
Open: `http://localhost:8080/dashboard.html`

Should show: **Active Workers: 5**

---

## ✅ Success Criteria - Test Checklist

- [ ] Broker UDP listener started on port 5001
- [ ] Worker 1 sent registration and received ACK
- [ ] Worker 2 sent registration and received ACK
- [ ] Worker 3 sent registration and received ACK
- [ ] Worker 4 sent registration and received ACK
- [ ] Worker 5 sent registration and received ACK
- [ ] Broker logged 5 "Received UDP message" entries
- [ ] Broker logged 5 "Worker registered in memory" entries
- [ ] Web API (`/api/workers`) shows 5 workers
- [ ] Dashboard shows 5 active workers

---

## 📊 UDP Communication Flow Diagram

```
Worker 1 (Port 6000)          Broker (UDP Port 5001)
     |                                |
     |------ REGISTER:6000 --------->| (UDP Packet)
     |                                | 
     |                                | [TaskManager.registerWorker()]
     |                                | [Store: 127.0.0.1:6000]
     |                                |
     |<------ REGISTERED ------------|  (UDP Acknowledgment)
     |                                |
     ✓ Registration Complete!         ✓ Worker Registered!

(Same process for Workers 2, 3, 4, 5)
```

---

## 🐛 Troubleshooting

### Problem: Port 5001 already in use
**Solution:**
```powershell
# Find process using port 5001
netstat -ano | findstr :5001

# Kill the process
taskkill /PID <process_id> /F
```

### Problem: PowerShell Maven command errors
**Error:** `Unknown lifecycle phase ".mainClass=com.computenet.App"`

**Solution:** Use JAR-based method instead:
```powershell
.\test-m5-jar.ps1
```
Or run commands manually using JAR files (see Method 3 above).

### Problem: Workers can't connect to broker
**Solution:**
1. Ensure broker is started FIRST and shows "M5: UDP Listener started on port 5001"
2. Check Windows Firewall isn't blocking UDP port 5001
3. Verify broker host is correct (use `localhost` for local testing)

### Problem: No registration messages in broker logs
**Solution:**
1. Verify broker's UDP listener started successfully
2. Check workers are using correct port (5001)
3. Try disabling antivirus/firewall temporarily
4. Check network connectivity: `ping localhost`

### Problem: Worker receives no acknowledgment
**Solution:**
1. Check broker is running and listening
2. Worker has 5-second timeout - broker might be slow to start
3. Check for network issues or firewall blocking responses

---

## 🔬 Advanced Testing

### Test Heartbeat Messages (After Registration)
Wait 10 seconds after workers start. You should see periodic heartbeats:
```
M5: Heartbeat received from 127.0.0.1
M5: Heartbeat received from 127.0.0.1
... (repeats every 10 seconds from each worker)
```

### Test Re-registration
Stop and restart workers to verify the system handles re-registration:
1. Stop a worker (Ctrl+C)
2. Restart the same worker
3. Verify it re-registers successfully

---

## 📝 Code Implementation Details

### Key Files

1. **WorkerUdpListener.java** - Broker UDP listener
   - Location: `src/main/java/com/computenet/broker/server/WorkerUdpListener.java`
   - Port: 5001

2. **WorkerClient.java** - Worker registration logic
   - Location: `src/main/java/com/computenet/client/WorkerClient.java`
   - Method: `registerWithBroker()` (lines 56-79)

3. **TaskManager.java** - Worker pool management
   - Location: `src/main/java/com/computenet/broker/service/TaskManager.java`
   - Method: `registerWorker()` (line 53-56)

### Registration Handler Code (WorkerUdpListener.java)
```java
if (message.startsWith("REGISTER:")) {
    String[] parts = message.split(":");
    if (parts.length >= 2) {
        int tcpPort = Integer.parseInt(parts[1]);
        taskManager.registerWorker(clientAddress.getHostAddress(), tcpPort);
        
        String ack = "REGISTERED";
        byte[] ackData = ack.getBytes();
        DatagramPacket ackPacket = new DatagramPacket(
            ackData, ackData.length, clientAddress, clientPort
        );
        datagramSocket.send(ackPacket);
    }
}
```

### Worker Registration Code (WorkerClient.java)
```java
private void registerWithBroker() throws IOException {
    DatagramSocket socket = new DatagramSocket();
    socket.setSoTimeout(5000); // 5-second timeout
    
    String message = "REGISTER:" + workerPort;
    byte[] sendData = message.getBytes();
    DatagramPacket sendPacket = new DatagramPacket(
        sendData, sendData.length, 
        InetAddress.getByName(brokerHost), 5001
    );
    
    socket.send(sendPacket);
    System.out.println("Worker: Sent registration to broker");
    
    // Wait for acknowledgment
    byte[] receiveData = new byte[1024];
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    socket.receive(receivePacket);
    
    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
    System.out.println("Worker: Registration acknowledged: " + response);
    
    socket.close();
}
```

---

## 🔄 Cleanup

To stop all running processes:
```powershell
# Stop all Java processes
Get-Process | Where-Object {$_.ProcessName -eq "java"} | Stop-Process -Force
```

Or close each terminal window manually with `Ctrl+C`.

---

## 🎓 Learning Outcomes

After completing this test, you will have verified:
- ✅ UDP datagram communication between distributed components
- ✅ Worker registration and acknowledgment protocol
- ✅ In-memory worker pool management
- ✅ Multi-client concurrent registration handling
- ✅ RESTful API integration for system monitoring
- ✅ Real-time dashboard updates via WebSocket

---

## 📝 Notes

- UDP is connectionless, so no persistent connection is maintained
- Registration is immediate and lightweight
- Heartbeats (every 10 seconds) ensure workers are still alive
- Workers can re-register if they restart
- Broker maintains worker pool in TaskManager (in-memory ConcurrentHashMap)

---

## 📚 Related Documentation

- **README.md** - Full project overview and architecture
- **pom.xml** - Maven dependencies and build configuration
- **Test Scripts:**
  - `test-m5-udp-registration.ps1` - Automated testing script (uses Maven exec:java)
  - Manual JAR-based testing (see Method 2 in Quick Test Methods)
  - Manual Maven testing (see Method 4 in Quick Test Methods)

---

**Last Updated:** 2024  
**Version:** 1.0 (Consolidated from M5-QUICKSTART.md)  
**Author:** Distributed Task Broker Project Team

