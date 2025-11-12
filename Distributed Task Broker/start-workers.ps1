# Start All 5 Workers
# Opens 5 separate terminal windows, one for each worker

Write-Host "========================================"
Write-Host "Starting 5 Worker Clients..."
Write-Host "========================================"
Write-Host ""

# Check if ports are available
Write-Host "Checking port availability..."
$portsInUse = @()
$workerPorts = @(6000, 6001, 6002, 6003, 6004, 7000, 7001, 7002, 7003, 7004)

foreach ($port in $workerPorts) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connection) {
        $portsInUse += $port
    }
}

if ($portsInUse.Count -gt 0) {
    Write-Host "ERROR: The following ports are already in use:" -ForegroundColor Red
    foreach ($port in $portsInUse) {
        Write-Host "  - Port $port"
    }
    Write-Host ""
    Write-Host "Run cleanup-ports.ps1 to kill processes using these ports."
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "All ports available!"
Write-Host ""
Write-Host "Each worker will:"
Write-Host "  - Listen on TCP port (6000-6004) for sub-tasks"
Write-Host "  - Start HTTP server (7000-7004) for web UI"
Write-Host "  - Register with broker via UDP (port 5001)"
Write-Host "  - Connect to NIO broadcast (port 5002)"
Write-Host "  - M4 Multicast: DISABLED by default (enable via dashboard)"
Write-Host ""
Write-Host "Opening 5 terminal windows..."
Write-Host ""

# Start Worker 1
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; Write-Host 'Worker 1 (TCP:6000, HTTP:7000)'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6000"
Start-Sleep -Milliseconds 500

# Start Worker 2
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; Write-Host 'Worker 2 (TCP:6001, HTTP:7001)'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6001"
Start-Sleep -Milliseconds 500

# Start Worker 3
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; Write-Host 'Worker 3 (TCP:6002, HTTP:7002)'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6002"
Start-Sleep -Milliseconds 500

# Start Worker 4
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; Write-Host 'Worker 4 (TCP:6003, HTTP:7003)'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6003"
Start-Sleep -Milliseconds 500

# Start Worker 5
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; Write-Host 'Worker 5 (TCP:6004, HTTP:7004)'; java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6004"

Write-Host ""
Write-Host "All 5 workers started!"
Write-Host ""
Write-Host "Worker Dashboards:"
Write-Host "  Worker 1: http://localhost:7000"
Write-Host "  Worker 2: http://localhost:7001"
Write-Host "  Worker 3: http://localhost:7002"
Write-Host "  Worker 4: http://localhost:7003"
Write-Host "  Worker 5: http://localhost:7004"
Write-Host ""
Write-Host "Broker UI: http://localhost:8080"
Write-Host ""
Write-Host "Press any key to exit this window..."
Read-Host
