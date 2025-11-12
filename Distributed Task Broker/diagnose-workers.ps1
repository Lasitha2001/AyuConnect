# Diagnostic Script - Check Worker Status
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Worker Status Diagnostic" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Checking which worker ports are listening..." -ForegroundColor Yellow
Write-Host ""

$ports = 6000..6004
$listening = @()

foreach ($port in $ports) {
    $result = netstat -ano | Select-String ":$port.*LISTENING"
    if ($result) {
        Write-Host "[OK] Port $port is LISTENING" -ForegroundColor Green
        $listening += $port
    } else {
        Write-Host "[FAIL] Port $port is NOT listening" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Summary: $($listening.Count) of 5 workers are running" -ForegroundColor $(if ($listening.Count -eq 5) {"Green"} else {"Yellow"})
Write-Host ""

if ($listening.Count -lt 5) {
    Write-Host "Missing workers on ports:" -ForegroundColor Red
    foreach ($port in $ports) {
        if ($port -notin $listening) {
            Write-Host "  - Port $port (Worker $($port - 5999))" -ForegroundColor Red
        }
    }
    Write-Host ""
    Write-Host "Possible solutions:" -ForegroundColor Yellow
    Write-Host "  1. Check if worker windows opened successfully" -ForegroundColor White
    Write-Host "  2. Look for error messages in worker terminals" -ForegroundColor White
    Write-Host "  3. Verify ports are not blocked by firewall" -ForegroundColor White
    Write-Host "  4. Try manual worker startup:" -ForegroundColor White
    Write-Host "     java -cp target\ComputeNet-Project-1.0.jar com.computenet.client.WorkerClient localhost 6000" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Checking for Java processes..." -ForegroundColor Yellow
$javaProcs = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcs) {
    Write-Host "Found $($javaProcs.Count) Java process(es):" -ForegroundColor Green
    $javaProcs | Select-Object Id, ProcessName, StartTime | Format-Table
} else {
    Write-Host "No Java processes found!" -ForegroundColor Red
}
