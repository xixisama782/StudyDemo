@echo off
setlocal
set GAMECENTER_FRONTEND_PORT=10109
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-with-redis.ps1" %*
