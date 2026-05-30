@echo off
setlocal
title GameCenter Backend Launcher
set GAMECENTER_FRONTEND_PORT=10109
cd /d "%~dp0"
echo Starting GameCenter backend with Redis and frontend dev server...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-with-redis.ps1"
echo.
echo Backend process exited. Press any key to close this window.
pause >nul
