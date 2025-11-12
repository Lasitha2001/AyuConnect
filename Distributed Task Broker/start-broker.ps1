# Start Broker Server
# This starts the main broker server which includes all M1-M5 modules

Write-Host "========================================"
Write-Host "Starting Broker Server..."
Write-Host "========================================"
Write-Host ""

# Check if broker ports are available
Write-Host "Checking port availability..."
$portsInUse = @()
$brokerPorts = @(5000, 5001, 5002, 8080)

foreach ($port in $brokerPorts) {
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connection) {
        $portsInUse += $port
    }
}

if ($portsInUse.Count -gt 0) {
    Write-Host "ERROR: The following ports are already in use:"
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
Write-Host "Modules Starting:"
Write-Host "  - M1: TCP Task Receiver (port 5000)"
Write-Host "  - M2: Multi-threading Executor (10 threads)"
Write-Host "  - M3: NIO Broadcast Handler (port 5002)"
Write-Host "  - M4: Multicast Config Broadcaster (230.0.0.1:6005)"
Write-Host "  - M5: UDP Worker Listener (port 5001)"
Write-Host ""
Write-Host "Web Interfaces:"
Write-Host "  - Broker UI:   http://localhost:8080"
Write-Host "  - Submit Task: http://localhost:8080/index.html"
Write-Host "  - Dashboard:   http://localhost:8080/dashboard.html"
Write-Host ""
Write-Host "Press Ctrl+C to stop the server"
Write-Host "========================================"
Write-Host ""

# Run the broker server
java -cp target\ComputeNet-Project-1.0.jar com.computenet.broker.server.BrokerServer
