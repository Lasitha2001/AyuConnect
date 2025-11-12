# M4 Multicast Task Config Broadcast Test
# Tests manual multicast control with localStorage persistence

Write-Host "========================================"
Write-Host "M4: Multicast Task Config Test"
Write-Host "========================================"
Write-Host ""

# Check if JAR exists
if (-not (Test-Path "target\ComputeNet-Project-1.0.jar")) {
    Write-Host "ERROR: JAR file not found!"
    Write-Host "Run: mvn clean package -DskipTests"
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Step 1: Starting Broker Server..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\start-broker.ps1"
Start-Sleep -Seconds 8

Write-Host "Step 2: Starting 5 Workers..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\start-workers.ps1"
Start-Sleep -Seconds 5

Write-Host "Step 3: Open worker dashboards and enable M4 on some workers:"
Write-Host "  - http://localhost:7000 (Worker 1) - Check the M4 checkbox"
Write-Host "  - http://localhost:7001 (Worker 2) - Check the M4 checkbox"
Write-Host "  - http://localhost:7002 (Worker 3) - Leave UNCHECKED"
Write-Host ""
Start-Sleep -Seconds 3

Write-Host "Step 4: Submit a task to trigger M4 broadcast..."
Write-Host ""
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; Write-Host 'Submitting Task for M4 Test'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.OriginatorClient localhost 'Test Task: Image processing pipeline'"

Write-Host ""
Write-Host "EXPECTED OUTCOME:"
Write-Host "========================================"
Write-Host "Broker Console should show:"
Write-Host "  - M4: Broadcasting task config to multicast group..."
Write-Host "  - M4: Task config broadcast complete"
Write-Host ""
Write-Host "Worker 1 & 2 Consoles (M4 ENABLED) should show:"
Write-Host "  - M4: Multicast listener started on 230.0.0.1:6005"
Write-Host "  - M4: Received task config: TASKCONFIG:1001:Test Task..."
Write-Host "  - M4: Stored config for task 1001 (5 splits, 5 sub-tasks)"
Write-Host ""
Write-Host "Worker 3 Console (M4 DISABLED) should show:"
Write-Host "  - (No M4 output - multicast listener not started)"
Write-Host ""
Write-Host "Worker Dashboards:"
Write-Host "  - Workers 1 & 2: 'Task Configs Received' tab shows config"
Write-Host "  - Worker 3: 'Task Configs Received' tab is EMPTY"
Write-Host ""
Write-Host "Test M4 module by:"
Write-Host "  1. Checking worker dashboards for config display"
Write-Host "  2. Verifying localStorage persistence (refresh page)"
Write-Host "  3. Observing selective multicast reception"
Write-Host ""
Write-Host "Press any key to exit..."
Read-Host
