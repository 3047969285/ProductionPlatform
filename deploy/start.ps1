# DevFlow 一键启动（需已安装 Docker Desktop）
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)
Write-Host "正在构建并启动 DevFlow（MySQL + 应用）..." -ForegroundColor Cyan
docker compose up -d --build
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host ""
Write-Host "部署完成" -ForegroundColor Green
Write-Host "本机访问: http://localhost:8080"
Write-Host "默认账号: admin / admin123"
Write-Host "查看日志: docker compose logs -f app"
