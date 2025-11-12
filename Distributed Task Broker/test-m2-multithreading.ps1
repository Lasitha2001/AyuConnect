# M2 Multi-threading Task Processing Test Script
# This script tests concurrent task processing using ExecutorService
# Splits tasks into 5 sub-tasks and dispatches to 5 workers

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "M2 Multi-threading Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if JAR exists
if (-not (Test-Path "target\ComputeNet-Project-1.0.jar")) {
    Write-Host "JAR file not found. Building project..." -ForegroundColor Yellow
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Build failed!" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
}

Write-Host "Step 1: Starting Broker Server..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# Start Broker Server
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar target\ComputeNet-Project-1.0.jar" -WindowStyle Normal

Write-Host "Waiting 10 seconds for Broker to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "Step 2: Starting 5 Worker Clients..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# Start 5 Workers (as per requirement)
Write-Host "Starting Worker 1 (Port 6000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6000" -WindowStyle Normal
Start-Sleep -Seconds 2

Write-Host "Starting Worker 2 (Port 6001)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6001" -WindowStyle Normal
Start-Sleep -Seconds 2

Write-Host "Starting Worker 3 (Port 6002)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6002" -WindowStyle Normal
Start-Sleep -Seconds 2

Write-Host "Starting Worker 4 (Port 6003)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6003" -WindowStyle Normal
Start-Sleep -Seconds 2

Write-Host "Starting Worker 5 (Port 6004)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6004" -WindowStyle Normal

Write-Host ""
Write-Host "Waiting 5 seconds for all workers to register..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "Verifying all workers are listening..." -ForegroundColor Yellow
$ports = 6000..6004
$allListening = $true
foreach ($port in $ports) {
    $result = netstat -ano | Select-String ":$port.*LISTENING"
    if ($result) {
        Write-Host "  [OK] Worker on port $port is listening" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Worker on port $port is NOT listening!" -ForegroundColor Red
        $allListening = $false
    }
}

if (-not $allListening) {
    Write-Host ""
    Write-Host "WARNING: Not all workers are ready!" -ForegroundColor Red
    Write-Host "Check worker terminals for errors." -ForegroundColor Yellow
    Write-Host "Press Enter to continue anyway, or Ctrl+C to abort..." -ForegroundColor Yellow
    Read-Host
}

Write-Host ""
Write-Host "Step 3: Submitting Task to Broker..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# Submit a task that will be split into 5 sub-tasks
Write-Host "Submitting task: 'Process large dataset with 1000000 records'..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost 'Process large dataset with 1000000 records'" -WindowStyle Normal

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "M2 Test Running!" -ForegroundColor Green
Write-Host ""
Write-Host "Expected Behavior:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. BROKER SERVER Terminal:" -ForegroundColor Cyan
Write-Host "   - M1: Received task data: Process large dataset..." -ForegroundColor White
Write-Host "   - M1: Task accepted with ID: XXXX" -ForegroundColor White
Write-Host "   - M2: Starting multi-threaded task processing" -ForegroundColor White
Write-Host "   - M2: Splitting task into 5 sub-tasks..." -ForegroundColor White
Write-Host "   - M2: Thread 1 created for sub-task dispatch..." -ForegroundColor White
Write-Host "   - M2: Thread 2 created for sub-task dispatch..." -ForegroundColor White
Write-Host "   - M2: Thread 3 created for sub-task dispatch..." -ForegroundColor White
Write-Host "   - M2: Thread 4 created for sub-task dispatch..." -ForegroundColor White
Write-Host "   - M2: Thread 5 created for sub-task dispatch..." -ForegroundColor White
Write-Host "   - M2: All 5 sub-tasks submitted to ExecutorService" -ForegroundColor White
Write-Host "   - M2: [Thread pool-X-thread-X] Dispatching sub-task X..." -ForegroundColor White
Write-Host "   - M2: [Thread pool-X-thread-X] Sub-task X sent to worker" -ForegroundColor White
Write-Host "   - M2: [Thread pool-X-thread-X] Sub-task X acknowledged..." -ForegroundColor White
Write-Host ""
Write-Host "2. WORKER 1-5 Terminals (Each shows):" -ForegroundColor Cyan
Write-Host "   - Worker: TCP Server listening on port 600X for sub-tasks" -ForegroundColor White
Write-Host "   - Worker: Registration acknowledged: REGISTERED" -ForegroundColor White
Write-Host "   - Worker: Broker connected to send sub-task" -ForegroundColor White
Write-Host "   - ========================================" -ForegroundColor White
Write-Host "   - Worker: Received sub-task from broker" -ForegroundColor White
Write-Host "   - Worker: Sub-task data: TASK:XXXX:SUBTASK:X:SubTask-X/5..." -ForegroundColor White
Write-Host "   - ========================================" -ForegroundColor White
Write-Host "   - Worker: Processing sub-task X for task XXXX" -ForegroundColor White
Write-Host "   - Worker: Sub-task X processing complete!" -ForegroundColor White
Write-Host "   - Worker: Sent acknowledgment: ACK:SUBTASK:X:COMPLETED" -ForegroundColor White
Write-Host ""
Write-Host "3. ORIGINATOR CLIENT Terminal:" -ForegroundColor Cyan
Write-Host "   - Originator Client starting..." -ForegroundColor White
Write-Host "   - Submitting task to broker: Process large dataset..." -ForegroundColor White
Write-Host "   - Broker response: TASK_ACCEPTED:XXXX" -ForegroundColor White
Write-Host "   - Task submitted successfully with ID: XXXX" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Success Criteria:" -ForegroundColor Green
Write-Host "  [OK] M1 receives task from originator" -ForegroundColor White
Write-Host "  [OK] M2 creates 5 threads using ExecutorService" -ForegroundColor White
Write-Host "  [OK] Task split into 5 sub-tasks" -ForegroundColor White
Write-Host "  [OK] Each sub-task dispatched via NEW TCP connection" -ForegroundColor White
Write-Host "  [OK] All 5 workers receive their respective sub-task" -ForegroundColor White
Write-Host "  [OK] Workers process sub-tasks concurrently" -ForegroundColor White
Write-Host "  [OK] Workers send acknowledgments back" -ForegroundColor White
Write-Host "  [OK] Sub-task results recorded in TaskManager" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Press Ctrl+C in each window to stop." -ForegroundColor Yellow
