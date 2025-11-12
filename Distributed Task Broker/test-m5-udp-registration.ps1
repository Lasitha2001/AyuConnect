# M5 UDP Worker Registration Test Script (PowerShell)
# Tests UDP-based worker registration and heartbeat monitoring
# This script starts the Broker Server and 5 Worker Clients

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "M5 UDP Worker Registration Test" -ForegroundColor Cyan
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

Write-Host "Starting Broker Server..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# Start Broker Server in new window
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; java -jar target\ComputeNet-Project-1.0.jar" -WindowStyle Normal

# Wait for broker to start
Write-Host "Waiting 5 seconds for Broker to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "Starting 5 Worker Clients..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# Start Worker 1
Write-Host "Starting Worker 1 (Port 6000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6000" -WindowStyle Normal
Start-Sleep -Seconds 1

# Start Worker 2
Write-Host "Starting Worker 2 (Port 6001)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6001" -WindowStyle Normal
Start-Sleep -Seconds 1

# Start Worker 3
Write-Host "Starting Worker 3 (Port 6002)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6002" -WindowStyle Normal
Start-Sleep -Seconds 1

# Start Worker 4
Write-Host "Starting Worker 4 (Port 6003)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6003" -WindowStyle Normal
Start-Sleep -Seconds 1

# Start Worker 5
Write-Host "Starting Worker 5 (Port 6004)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6004" -WindowStyle Normal

Write-Host ""
Write-Host "Waiting 3 seconds for all workers to register..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "M5 Test Running!" -ForegroundColor Green
Write-Host ""
Write-Host "Expected Behavior:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. BROKER SERVER Terminal:" -ForegroundColor Cyan
Write-Host "   - M5: UDP Listener started on port 5001" -ForegroundColor White
Write-Host "   - M5: Received UDP message: REGISTER:6000 from 127.0.0.1:xxxxx" -ForegroundColor White
Write-Host "   - TaskManager: Worker registered in memory: 127.0.0.1:6000" -ForegroundColor White
Write-Host "   - M5: Received UDP message: REGISTER:6001 from 127.0.0.1:xxxxx" -ForegroundColor White
Write-Host "   - TaskManager: Worker registered in memory: 127.0.0.1:6001" -ForegroundColor White
Write-Host "   - (Repeat for workers 6002, 6003, 6004)" -ForegroundColor White
Write-Host "   - M5: Heartbeat received from 127.0.0.1 (every 10 seconds)" -ForegroundColor White
Write-Host ""
Write-Host "2. WORKER 1-5 Terminals (Each shows):" -ForegroundColor Cyan
Write-Host "   - Worker: Sent registration to broker" -ForegroundColor White
Write-Host "   - Worker: Registration acknowledged: REGISTERED" -ForegroundColor White
Write-Host "   - (Heartbeat messages sent every 10 seconds)" -ForegroundColor White
Write-Host ""
Write-Host "3. Optional - Verify via Web UI:" -ForegroundColor Cyan
Write-Host "   - Open browser: http://localhost:8080/index.html" -ForegroundColor White
Write-Host "   - Check that 'Number of Sub-Tasks' dropdown shows 5 options (1-5)" -ForegroundColor White
Write-Host "   - This confirms workers are registered and visible to the system" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Success Criteria:" -ForegroundColor Green
Write-Host "  [OK] Broker UDP listener started on port 5001" -ForegroundColor White
Write-Host "  [OK] All 5 workers sent UDP registration messages" -ForegroundColor White
Write-Host "  [OK] Broker received and acknowledged all 5 registrations" -ForegroundColor White
Write-Host "  [OK] Workers stored in TaskManager with correct addresses" -ForegroundColor White
Write-Host "  [OK] Heartbeat messages received periodically (every 10 seconds)" -ForegroundColor White
Write-Host ""
Write-Host "Verification via Web API:" -ForegroundColor Cyan
Write-Host "  http://localhost:8080/api/workers" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Press Ctrl+C in each window to stop the processes manually." -ForegroundColor Yellow
