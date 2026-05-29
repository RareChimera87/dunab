@echo off
chcp 65001 >nul
title DUNAB — Iniciar Aplicacion

:: ═══════════════════════════════════════════════════════
::  DUNAB — Script de inicio  (doble clic para ejecutar)
::  1. Levanta PostgreSQL con Docker
::  2. Ejecuta el backend con Maven (o Maven Wrapper)
:: ═══════════════════════════════════════════════════════

echo.
echo  ╔═══════════════════════════════════════╗
echo  ║   DUNAB — Iniciando aplicacion...    ║
echo  ╚═══════════════════════════════════════╝
echo.

set "PROYECTO_DIR=%~dp0.."
set "BACKEND_DIR=%~dp0..\backend"

:: ── 1. Verificar Java ──────────────────────────────────
echo [1/4] Verificando Java 17+...
java -version >nul 2>&1
if errorlevel 1 (
    echo  ERROR: Java no encontrado.
    echo         Ejecuta primero:  instalar.ps1  (clic derecho → Ejecutar con PowerShell)
    pause & exit /b 1
)
echo       Java OK

:: ── 2. Detectar comando Maven ──────────────────────────
echo [2/4] Detectando Maven...
set "MVN_CMD=mvn"
mvn --version >nul 2>&1
if errorlevel 1 (
    echo       mvn no encontrado en PATH, usando Maven Wrapper (mvnw.cmd)...
    set "MVN_CMD=%BACKEND_DIR%\mvnw.cmd"
)
echo       Maven: %MVN_CMD%

:: ── 3. Levantar PostgreSQL con Docker ──────────────────
echo [3/4] Levantando base de datos...
docker --version >nul 2>&1
if errorlevel 1 (
    echo  AVISO: Docker no disponible. Usando H2 en memoria (datos no persisten entre reinicios).
    set "SPRING_PROFILE=-Dspring.profiles.active=dev"
    goto :iniciar_backend
)

cd /d "%PROYECTO_DIR%"
docker info >nul 2>&1

if errorlevel 1 (
    echo  AVISO: Docker Desktop no esta corriendo. Abrelo y vuelve a ejecutar este script.
    echo         O usa H2 temporalmente: set SPRING_PROFILE=-Dspring.profiles.active=dev
    echo.
    echo  Iniciando Docker Desktop...
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe" 2>nul
    timeout /t 15 /nobreak >nul
    echo  Intentando de nuevo...
    docker info >nul 2>&1
    if errorlevel 1 (
        echo  Docker sigue sin responder. Usando H2 en memoria.
        set "SPRING_PROFILE=-Dspring.profiles.active=dev"
        goto :iniciar_backend
    )
)

docker compose -f "%~dp0..\infra\docker-compose.yml" up -d
if errorlevel 1 (
    echo  Usando H2 en memoria como alternativa...
    set "SPRING_PROFILE=-Dspring.profiles.active=dev"
) else (
    echo       PostgreSQL listo.
    set "SPRING_PROFILE="
    timeout /t 4 /nobreak >nul
)

:iniciar_backend
:: ── 4. Ejecutar el backend ─────────────────────────────
echo [4/4] Compilando e iniciando Spring Boot...
echo.
echo  ┌─────────────────────────────────────────────────────┐
echo  │  Espera hasta ver: "Started DunabApplication"      │
echo  │  (La primera vez descarga dependencias: ~3 min)     │
echo  └─────────────────────────────────────────────────────┘
echo.
echo  Una vez iniciado:
echo    Frontend  →  Doble clic en  abrir-frontend.bat
echo    API REST  →  http://localhost:8080/api
echo    Swagger   →  http://localhost:8080/api/swagger-ui.html
echo.
echo  Credenciales admin: admin@unab.edu.co / Admin1234!
echo.
echo  (Ctrl+C para detener el backend)
echo  ────────────────────────────────────────────────────────
echo.

cd /d "%BACKEND_DIR%"
"%MVN_CMD%" spring-boot:run %SPRING_PROFILE%

echo.
echo  Backend detenido.
pause
