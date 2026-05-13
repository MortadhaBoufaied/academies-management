<#
.SYNOPSIS
    Sets up a unified Python virtual environment for all services.
.DESCRIPTION
    Creates a venv, activates it, and installs requirements for:
      - scouting-ai-service (FastAPI)
      - chatbot (Django)
      - admin platform (FastAPI)
#>

param(
    [switch]$SkipVenv  # Use if you already have a venv active
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot  # d:\master_pfe

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Environment Setup – All Services" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# ── 1. Virtual environment ──────────────────────────────────────────────
$VenvPath = Join-Path $Root "venv"

if (-not $SkipVenv) {
    if (Test-Path $VenvPath) {
        Write-Host "[OK] Virtual environment already exists at $VenvPath" -ForegroundColor Green
    } else {
        Write-Host "[..] Creating virtual environment..." -ForegroundColor Yellow
        python -m venv $VenvPath
        Write-Host "[OK] Virtual environment created." -ForegroundColor Green
    }

    # Activate
    $ActivateScript = Join-Path $VenvPath "Scripts\Activate.ps1"
    if (Test-Path $ActivateScript) {
        Write-Host "[..] Activating venv..." -ForegroundColor Yellow
        & $ActivateScript
        Write-Host "[OK] venv activated." -ForegroundColor Green
    } else {
        Write-Host "[!!] Could not find $ActivateScript" -ForegroundColor Red
        exit 1
    }
}

# ── 2. Install requirements ─────────────────────────────────────────────
Write-Host ""
Write-Host "[..] Installing scouting-ai-service requirements..." -ForegroundColor Yellow
pip install -r (Join-Path $Root "scouting-ai-service\requirements.txt") --quiet
Write-Host "[OK] scouting-ai-service requirements installed." -ForegroundColor Green

Write-Host "[..] Installing chatbot requirements..." -ForegroundColor Yellow
pip install -r (Join-Path $Root "chatbot\chatbot\requirements.txt") --quiet
Write-Host "[OK] chatbot requirements installed." -ForegroundColor Green

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "  Environment ready!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next: run .\run_all.ps1 to start all services."
