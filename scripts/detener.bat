@echo off
chcp 65001 >nul
title DUNAB — Detener Aplicacion

echo.
echo  Deteniendo DUNAB...
echo.

:: Matar proceso Java del backend
echo [1/2] Deteniendo backend...
for /f "tokens=5" %%p in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do (
    taskkill /PID %%p /F >nul 2>&1
)
echo       Backend detenido.

:: Detener contenedores Docker
echo [2/2] Deteniendo base de datos (Docker)...
docker compose -f "%~dp0..\infra\docker-compose.yml" down >nul 2>&1
echo       Base de datos detenida.

echo.
echo  DUNAB detenido correctamente.
echo.
pause
