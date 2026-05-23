param(
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $root "target\enterprise-hr-ai-agent-0.0.1-SNAPSHOT.jar"
$stdout = Join-Path $root "local-run.log"
$stderr = Join-Path $root "local-run.err.log"

if (-not (Test-Path $jar)) {
    Push-Location $root
    try {
        mvn "-Dmaven.test.skip=true" package
    }
    finally {
        Pop-Location
    }
}

$process = Start-Process `
    -FilePath java `
    -ArgumentList @("-jar", "`"$jar`"", "--spring.profiles.active=local", "--server.port=$Port") `
    -WorkingDirectory $root `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -WindowStyle Hidden `
    -PassThru

try {
    $baseUrl = "http://localhost:$Port"
    $healthy = $false

    for ($i = 0; $i -lt 30; $i++) {
        try {
            $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -TimeoutSec 2
            if ($health.status -eq "UP") {
                $healthy = $true
                break
            }
        }
        catch {
            Start-Sleep -Seconds 1
        }
    }

    if (-not $healthy) {
        throw "Service did not become healthy. Check local-run.log and local-run.err.log."
    }

    $employees = Invoke-RestMethod -Uri "$baseUrl/api/v1/employees?keyword=%E7%A0%94%E5%8F%91" -TimeoutSec 5
    if (-not $employees -or $employees[0].annualLeaveBalance -ne 10) {
        throw "Employee smoke test failed."
    }

    "Smoke test passed. Health is UP and employee API returned seeded data."
}
finally {
    if (-not $process.HasExited) {
        Stop-Process -Id $process.Id -Force
    }
}
