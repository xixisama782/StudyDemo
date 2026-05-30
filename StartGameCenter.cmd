@echo off
setlocal
title GameCenter Launcher
set GAMECENTER_FRONTEND_PORT=10109
cd /d "%~dp0backend"
echo Starting GameCenter backend, frontend and Redis...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0backend\start-with-redis.ps1"
echo.
echo GameCenter process exited. Press any key to close this window.
pause >nul
