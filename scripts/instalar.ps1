#Requires -Version 5.1
<#
.SYNOPSIS
    Instalador automático de DUNAB — Gestión de Dinero UNAB
    Instala: Java 17, Maven 3.9, Docker Desktop
    Ejecutar como ADMINISTRADOR: clic derecho → "Ejecutar con PowerShell"
#>

$ErrorActionPreference = "Stop"
$Host.UI.RawUI.WindowTitle = "DUNAB Installer"

function Write-Step  { param($msg) Write-Host "`n▶  $msg" -ForegroundColor Cyan }
function Write-OK    { param($msg) Write-Host "   ✓  $msg" -ForegroundColor Green }
function Write-Warn  { param($msg) Write-Host "   ⚠  $msg" -ForegroundColor Yellow }
function Write-Fail  { param($msg) Write-Host "   ✕  $msg" -ForegroundColor Red }

# ── Banner ────────────────────────────────────────────────
Clear-Host
Write-Host @"
╔══════════════════════════════════════════════╗
║   DUNAB — Instalador Automático              ║
║   Universidad Autónoma de Bucaramanga        ║
╚══════════════════════════════════════════════╝
"@ -ForegroundColor Magenta

# ── Verificar que se ejecuta como Admin ──────────────────
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole(
    [Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Fail "Debes ejecutar este script como ADMINISTRADOR."
    Write-Host "   Clic derecho en el archivo → 'Ejecutar con PowerShell' → 'Sí'" -ForegroundColor Yellow
    Read-Host "`nPresiona Enter para salir"
    exit 1
}

# ── Verificar winget ──────────────────────────────────────
Write-Step "Verificando winget (Windows Package Manager)..."
try {
    $wgVer = winget --version 2>&1
    Write-OK "winget disponible: $wgVer"
} catch {
    Write-Fail "winget no está disponible. Actualiza Windows 10/11 o instálalo desde Microsoft Store."
    Read-Host "`nPresiona Enter para salir"
    exit 1
}

# ═══════════════════════════════════════════════════════════
# 1. JAVA 17 (Eclipse Temurin)
# ═══════════════════════════════════════════════════════════
Write-Step "Verificando Java 17..."
$javaOk = $false
try {
    $jv = java -version 2>&1
    if ($jv -match "17\.|21\.|22\.|23\.") {
        Write-OK "Java ya instalado: $($jv[0])"
        $javaOk = $true
    }
} catch { }

if (-not $javaOk) {
    Write-Host "   Instalando Java 17 (Eclipse Temurin)..." -ForegroundColor White
    winget install --id EclipseAdoptium.Temurin.17.JDK `
        --accept-source-agreements --accept-package-agreements --silent

    # Actualizar PATH en sesión actual
    $env:JAVA_HOME = (Get-Item "C:\Program Files\Eclipse Adoptium\jdk-17*").FullName
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

    # Configurar JAVA_HOME permanentemente
    [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $env:JAVA_HOME, "Machine")
    $machinePath = [System.Environment]::GetEnvironmentVariable("PATH", "Machine")
    if ($machinePath -notlike "*Temurin*") {
        [System.Environment]::SetEnvironmentVariable("PATH", "$env:JAVA_HOME\bin;$machinePath", "Machine")
    }
    Write-OK "Java 17 instalado y configurado."
}

# ═══════════════════════════════════════════════════════════
# 2. MAVEN 3.9
# ═══════════════════════════════════════════════════════════
Write-Step "Verificando Apache Maven..."
$mavenOk = $false
try {
    $mv = mvn --version 2>&1
    if ($mv -match "Apache Maven") {
        Write-OK "Maven ya instalado: $($mv[0])"
        $mavenOk = $true
    }
} catch { }

if (-not $mavenOk) {
    Write-Host "   Instalando Apache Maven 3.9..." -ForegroundColor White

    # Intentar via winget primero
    $wingetMaven = winget install --id Apache.Maven `
        --accept-source-agreements --accept-package-agreements --silent 2>&1

    if ($LASTEXITCODE -ne 0) {
        # Fallback: descarga manual
        Write-Host "   Descargando Maven manualmente..." -ForegroundColor White
        $mavenUrl  = "https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
        $mavenZip  = "$env:TEMP\maven.zip"
        $mavenDest = "C:\Program Files\Apache\maven"

        Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip -UseBasicParsing
        Expand-Archive -Path $mavenZip -DestinationPath "C:\Program Files\Apache" -Force
        Rename-Item "C:\Program Files\Apache\apache-maven-3.9.6" $mavenDest -Force -ErrorAction SilentlyContinue

        $env:MAVEN_HOME = $mavenDest
        [System.Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenDest, "Machine")
        $machinePath = [System.Environment]::GetEnvironmentVariable("PATH", "Machine")
        [System.Environment]::SetEnvironmentVariable("PATH", "$mavenDest\bin;$machinePath", "Machine")
        $env:PATH = "$mavenDest\bin;$env:PATH"
    }
    Write-OK "Maven instalado."
}

# ═══════════════════════════════════════════════════════════
# 3. DOCKER DESKTOP
# ═══════════════════════════════════════════════════════════
Write-Step "Verificando Docker Desktop..."
$dockerOk = $false
try {
    $dv = docker --version 2>&1
    if ($dv -match "Docker version") {
        Write-OK "Docker ya instalado: $dv"
        $dockerOk = $true
    }
} catch { }

if (-not $dockerOk) {
    Write-Host "   Instalando Docker Desktop..." -ForegroundColor White
    Write-Warn "La instalación de Docker requiere reiniciar el equipo al finalizar."

    winget install --id Docker.DockerDesktop `
        --accept-source-agreements --accept-package-agreements --silent

    Write-OK "Docker Desktop instalado. Reinicia el equipo después de que termine este script."
}

# ═══════════════════════════════════════════════════════════
# 4. VERIFICACIÓN FINAL
# ═══════════════════════════════════════════════════════════
Write-Step "Verificación final..."

# Refrescar PATH
$env:PATH = [System.Environment]::GetEnvironmentVariable("PATH","Machine") + ";" +
            [System.Environment]::GetEnvironmentVariable("PATH","User")

$checks = @(
    @{ Name = "Java";   Cmd = "java -version 2>&1" },
    @{ Name = "Maven";  Cmd = "mvn --version 2>&1" },
    @{ Name = "Docker"; Cmd = "docker --version 2>&1" }
)

foreach ($c in $checks) {
    try {
        $out = Invoke-Expression $c.Cmd | Select-Object -First 1
        Write-OK "$($c.Name): $out"
    } catch {
        Write-Warn "$($c.Name): no encontrado en PATH (puede requerir reinicio)"
    }
}

# ═══════════════════════════════════════════════════════════
# 5. MENSAJE FINAL
# ═══════════════════════════════════════════════════════════
Write-Host @"

╔══════════════════════════════════════════════════════════╗
║  ✓ Instalación completada                                ║
║                                                          ║
║  SIGUIENTE PASO:                                         ║
║  1. Reinicia el equipo (necesario para Docker)           ║
║  2. Abre Docker Desktop y espera que inicie              ║
║  3. Ejecuta  iniciar.bat  como administrador             ║
╚══════════════════════════════════════════════════════════╝
"@ -ForegroundColor Green

Read-Host "`nPresiona Enter para salir"
