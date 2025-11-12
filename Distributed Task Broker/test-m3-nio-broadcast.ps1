# M3: Java NIO Broadcast Testing Script
# This script tests the non-blocking NIO broadcast functionality
# Expected output: Broker broadcasts task progress every 2 seconds to all connected workers

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "M3: Java NIO Broadcast Test" -ForegroundColor Cyan
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

# Step 1: Start the Broker Server
Write-Host "Step 1: Starting Broker Server..." -ForegroundColor Yellow
$broker = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.broker.server.BrokerServer" -PassThru -WindowStyle Normal
Write-Host "Broker started (PID: $($broker.Id)). Waiting 5 seconds for initialization..." -ForegroundColor Green
Start-Sleep -Seconds 5

# Step 2: Start 5 Workers (to register with broker and connect to NIO)
Write-Host ""
Write-Host "Step 2: Starting 5 Workers..." -ForegroundColor Yellow
$workers = @()
for ($i = 0; $i -le 4; $i++) {
    $port = 6000 + $i
    $worker = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost $port" -PassThru -WindowStyle Normal
    $workers += $worker
    Write-Host "Worker $($i+1) started on port $port (PID: $($worker.Id))" -ForegroundColor Green
    Start-Sleep -Milliseconds 500
}

Write-Host "All workers started. Waiting 3 seconds for NIO connections..." -ForegroundColor Green
Start-Sleep -Seconds 3

# Step 3: Submit a task via M1 TCP
Write-Host ""
Write-Host "Step 3: Submitting task via M1 TCP..." -ForegroundColor Yellow
$originator = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost 'Matrix multiplication dataset'" -PassThru -WindowStyle Normal
Write-Host "Task submitted (PID: $($originator.Id))" -ForegroundColor Green

# Step 4: Wait and observe NIO broadcasts
Write-Host ""
Write-Host "Step 4: Observing M3 NIO broadcasts..." -ForegroundColor Yellow
Write-Host "Expected behavior:" -ForegroundColor Cyan
Write-Host "  1. Broker logs: 'M3: Selector detected X ready keys' every 2 seconds" -ForegroundColor White
Write-Host "  2. Broker logs: 'M3: Broadcasting task progress update' every 2 seconds" -ForegroundColor White
Write-Host "  3. Broker logs: 'M3: Active NIO channels: 5'" -ForegroundColor White
Write-Host "  4. Workers receive: 'PROGRESS:XXXX:Processing...'" -ForegroundColor White
Write-Host ""
Write-Host "Monitoring for 15 seconds (should see ~7 broadcasts)..." -ForegroundColor Yellow

# Let the system run for 15 seconds to observe broadcasts
Start-Sleep -Seconds 15

# Step 5: Cleanup
Write-Host ""
Write-Host "Step 5: Test complete. Review the windows above for:" -ForegroundColor Yellow
Write-Host "  - Broker: M3 selector logs and broadcast messages" -ForegroundColor White
Write-Host "  - Workers: PROGRESS messages received via NIO" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to terminate all processes..." -ForegroundColor Red
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Cleanup
Write-Host "Cleaning up processes..." -ForegroundColor Yellow
Stop-Process -Id $broker.Id -Force -ErrorAction SilentlyContinue
foreach ($w in $workers) {
    Stop-Process -Id $w.Id -Force -ErrorAction SilentlyContinue
}
Stop-Process -Id $originator.Id -Force -ErrorAction SilentlyContinue

Write-Host "All processes terminated. Test complete." -ForegroundColor Green
