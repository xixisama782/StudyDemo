@echo off
setlocal
title GameCenter Client Frontend
set GAMECENTER_FRONTEND_PORT=10109
cd /d "%~dp0backend"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0backend\start-with-redis.ps1" -ClientFrontendOnly
pause >nul
