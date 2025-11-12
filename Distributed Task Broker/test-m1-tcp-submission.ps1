# M1 TCP Task Submission Test
# Tests reliable task submission via TCP blocking sockets on port 5000

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "M1: TCP Task Submission Test" -ForegroundColor Cyan
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
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; java -jar target\ComputeNet-Project-1.0.jar" -WindowStyle Normal

Write-Host "Broker started. Waiting 10 seconds for initialization..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "Step 2: Submitting test task via M1 TCP (port 5000)..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Submit task
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; Write-Host 'Originator Client - Submitting Task'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost 'Test Task: Process customer data'" -WindowStyle Normal

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "M1 Test Running!" -ForegroundColor Green
Write-Host ""
Write-Host "Expected Behavior:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. BROKER SERVER Terminal:" -ForegroundColor Cyan
Write-Host "   - M1: TCP Task Receiver listening on port 5000" -ForegroundColor White
Write-Host "   - M1: Originator connected. Handing off to Executor..." -ForegroundColor White
Write-Host "   - M1: Received task data: Test Task: Process customer data" -ForegroundColor White
Write-Host "   - M1: Task accepted with ID: [number]" -ForegroundColor White
Write-Host ""
Write-Host "2. ORIGINATOR CLIENT Terminal:" -ForegroundColor Cyan
Write-Host "   - Originator Client starting..." -ForegroundColor White
Write-Host "   - Submitting task to broker: Test Task: Process customer data" -ForegroundColor White
Write-Host "   - Task data sent to broker" -ForegroundColor White
Write-Host "   - Broker response: TASK_ACCEPTED:[number]" -ForegroundColor White
Write-Host "   - Task submitted successfully with ID: [number]" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Success Criteria:" -ForegroundColor Green
Write-Host "  [OK] Broker TCP listener started on port 5000" -ForegroundColor White
Write-Host "  [OK] Originator client connected" -ForegroundColor White
Write-Host "  [OK] Task received reliably via TCP" -ForegroundColor White
Write-Host "  [OK] Task acknowledged with TASK_ACCEPTED:ID" -ForegroundColor White
Write-Host "  [OK] Task stored in TaskManager with unique ID" -ForegroundColor White
Write-Host ""
Write-Host "Verification via Web API:" -ForegroundColor Cyan
Write-Host "  http://localhost:8080/api/workers" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Press Ctrl+C in each window to stop." -ForegroundColor Yellow
