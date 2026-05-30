@echo off
setlocal
title GameCenter Admin Frontend
set GAMECENTER_ADMIN_FRONTEND_PORT=10110
cd /d "%~dp0backend"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0backend\start-with-redis.ps1" -AdminFrontendOnly
pause >nul
