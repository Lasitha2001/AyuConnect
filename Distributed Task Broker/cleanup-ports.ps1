# Cleanup Script - Kills processes using broker and worker ports
# Run this if you get "Port already in use" errors

Write-Host "========================================"
Write-Host "Cleaning up ports..."
Write-Host "========================================"
Write-Host ""

$ports = @(5000, 5001, 5002, 6000, 6001, 6002, 6003, 6004, 7000, 7001, 7002, 7003, 7004, 8080)

foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($connections) {
        foreach ($conn in $connections) {
            $processId = $conn.OwningProcess
            $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
            if ($process) {
                Write-Host "Killing process on port $port (PID: $processId, Name: $($process.ProcessName))"
                Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
            }
        }
    }
}

Write-Host ""
Write-Host "Cleanup complete!"
Write-Host ""
Write-Host "You can now run:"
Write-Host "  .\start-broker.ps1"
Write-Host "  .\start-workers.ps1"
Write-Host ""
Write-Host "Press any key to exit..."
Read-Host
