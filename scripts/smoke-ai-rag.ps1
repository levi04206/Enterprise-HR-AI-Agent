param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$SampleDocument = ".\docs\samples\2026员工考勤管理办法.txt"
)

$ErrorActionPreference = "Stop"

function Invoke-JsonGet {
    param([string]$Url)
    $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -Method Get -TimeoutSec 60
    return $response.Content | ConvertFrom-Json
}

function Assert-StatusUp {
    param(
        [string]$Name,
        [object]$Response
    )
    if ($Response.status -ne "UP") {
        throw "$Name status is not UP. Actual response: $($Response | ConvertTo-Json -Depth 6)"
    }
}

Write-Host "== Enterprise HR AI Agent AI/RAG smoke test =="
Write-Host "BaseUrl: $BaseUrl"

if (-not (Test-Path -LiteralPath $SampleDocument)) {
    throw "Sample document not found: $SampleDocument"
}

if (-not $env:OPENAI_API_KEY) {
    Write-Warning "OPENAI_API_KEY is not visible to this PowerShell process. If the app is started from IDEA, this script can still call the running app, but diagnostics may fail if the app did not receive the variable."
}
if (-not $env:DASHSCOPE_API_KEY) {
    Write-Warning "DASHSCOPE_API_KEY is not visible to this PowerShell process. If the app is started from IDEA, this script can still call the running app, but diagnostics may fail if the app did not receive the variable."
}

Write-Host "1. Checking application health..."
$health = Invoke-JsonGet "$BaseUrl/actuator/health"
Assert-StatusUp "Actuator health" $health

Write-Host "2. Checking chat model..."
$chatDiagnostic = Invoke-JsonGet "$BaseUrl/api/v1/diagnostics/chat"
Assert-StatusUp "Chat diagnostic" $chatDiagnostic

Write-Host "3. Checking embedding model..."
$embeddingDiagnostic = Invoke-JsonGet "$BaseUrl/api/v1/diagnostics/embedding"
Assert-StatusUp "Embedding diagnostic" $embeddingDiagnostic

Write-Host "4. Uploading sample HR policy document..."
$ingestResponse = Invoke-RestMethod -Uri "$BaseUrl/api/v1/knowledge/ingest" `
    -Method Post `
    -Form @{ file = Get-Item -LiteralPath $SampleDocument } `
    -TimeoutSec 120
Write-Host "Indexed document id: $($ingestResponse.documentId), chunks: $($ingestResponse.chunkCount)"

Write-Host "5. Creating chat session..."
$session = Invoke-RestMethod -Uri "$BaseUrl/api/v1/chat/sessions" `
    -Method Post `
    -ContentType "application/json" `
    -Body (@{ title = "AI/RAG smoke test" } | ConvertTo-Json) `
    -TimeoutSec 60
Write-Host "Session id: $($session.id)"

Write-Host "6. Calling SSE chat endpoint..."
$chatBody = @{
    message = "根据公司制度，请假申请需要提前多久提交？"
    sessionId = $session.id
} | ConvertTo-Json

$chatResponse = Invoke-WebRequest -UseBasicParsing `
    -Uri "$BaseUrl/api/v1/chat/stream" `
    -Method Post `
    -ContentType "application/json" `
    -Body $chatBody `
    -TimeoutSec 180

if (-not $chatResponse.Content -or $chatResponse.Content.Length -lt 10) {
    throw "Chat stream response is empty or too short."
}

Write-Host "SSE response preview:"
Write-Host ($chatResponse.Content.Substring(0, [Math]::Min(500, $chatResponse.Content.Length)))

Write-Host "7. Checking persisted session messages..."
$messages = Invoke-RestMethod -Uri "$BaseUrl/api/v1/chat/sessions/$($session.id)/messages" `
    -Method Get `
    -TimeoutSec 60
if ($messages.Count -lt 2) {
    throw "Expected at least 2 persisted messages, actual count: $($messages.Count)"
}

Write-Host "AI/RAG smoke test passed."
