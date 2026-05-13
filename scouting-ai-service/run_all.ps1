<#
.SYNOPSIS
    Start all services: scouting-ai, chatbot, and admin platform.
.DESCRIPTION
    Launches each service in a separate background job so they
    all run simultaneously from a single terminal.
    Press Ctrl+C to stop all services.
#>

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot  # d:\master_pfe

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Starting All Services" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# ── Activate venv if present ─────────────────────────────────────────────
$VenvActivate = Join-Path $Root "venv\Scripts\Activate.ps1"
if (Test-Path $VenvActivate) {
    & $VenvActivate
    Write-Host "[OK] venv activated." -ForegroundColor Green
}

# ── Start services as background jobs ────────────────────────────────────

# 1. Scouting AI Service (port 8010)
$scoutingDir = Join-Path $Root "scouting-ai-service"
Write-Host "[..] Starting Scouting AI Service on port 8010..." -ForegroundColor Yellow
$scouting = Start-Job -ScriptBlock {
    param($dir, $venv)
    Set-Location $dir
    if (Test-Path $venv) { & $venv }
    python -m uvicorn app.main:app --host 0.0.0.0 --port 8010 --reload
} -ArgumentList $scoutingDir, $VenvActivate

# 2. Chatbot (Django, port 8020)
$chatbotDir = Join-Path $Root "chatbot\chatbot"
Write-Host "[..] Starting Chatbot Service on port 8020..." -ForegroundColor Yellow
$chatbot = Start-Job -ScriptBlock {
    param($dir, $venv)
    Set-Location $dir
    if (Test-Path $venv) { & $venv }
    python manage.py runserver 0.0.0.0:8020
} -ArgumentList $chatbotDir, $VenvActivate

# 3. Admin Platform (port 8000)
Write-Host "[..] Starting Admin Platform on port 8000..." -ForegroundColor Yellow
$admin = Start-Job -ScriptBlock {
    param($dir, $venv)
    Set-Location $dir
    if (Test-Path $venv) { & $venv }
    python -m uvicorn adminplatform.main:app --host 0.0.0.0 --port 8000 --reload
} -ArgumentList $scoutingDir, $VenvActivate

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "  All services starting!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Scouting AI : http://localhost:8010/docs" -ForegroundColor White
Write-Host "  Chatbot     : http://localhost:8020/" -ForegroundColor White
Write-Host "  Admin Panel : http://localhost:8000/admin/upload" -ForegroundColor White
Write-Host ""
Write-Host "Press Ctrl+C to stop all services." -ForegroundColor DarkGray
Write-Host ""

# ── Tail logs until user presses Ctrl+C ──────────────────────────────────
try {
    while ($true) {
        # Display any new output from each job
        @($scouting, $chatbot, $admin) | ForEach-Object {
            $output = Receive-Job -Job $_ -ErrorAction SilentlyContinue
            if ($output) {
                Write-Host $output
            }
        }
        Start-Sleep -Milliseconds 500
    }
} finally {
    Write-Host ""
    Write-Host "[..] Stopping all services..." -ForegroundColor Yellow
    $scouting, $chatbot, $admin | ForEach-Object {
        Stop-Job -Job $_ -ErrorAction SilentlyContinue
        Remove-Job -Job $_ -Force -ErrorAction SilentlyContinue
    }
    Write-Host "[OK] All services stopped." -ForegroundColor Green
}
