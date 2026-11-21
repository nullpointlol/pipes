# ==============================================================================
# PIPES - BLUESTACKS 5 AUTOMATED DEPLOYMENT & LAUNCH SCRIPT
# ==============================================================================
[CmdletBinding()]
param(
    [string]$ApkPath = "",
    [string]$Instance = "Tiramisu64",
    [switch]$AutoInstallJdk,
    [switch]$LaunchOnly
)

$ErrorActionPreference = "Stop"

function Log-Zen([string]$tag, [string]$msg, [string]$color = "Cyan") {
    Write-Host "[$tag] $msg" -ForegroundColor $color
}

Write-Host ""
Log-Zen "PIPES" "================================================" "Cyan"
Log-Zen "PIPES" "  PIPES - BLUESTACKS 5 ENGINE PIPELINE    " "Cyan"
Log-Zen "PIPES" "================================================" "Cyan"

# 1. LOCALIZAR BLUESTACKS 5 Y HD-ADB
$BlueStacksDir = "C:\Program Files\BlueStacks_nxt"
$PlayerExe     = Join-Path $BlueStacksDir "HD-Player.exe"
$HdAdbExe      = Join-Path $BlueStacksDir "HD-Adb.exe"
$ConfFile      = "C:\ProgramData\BlueStacks_nxt\bluestacks.conf"

if (-not (Test-Path $PlayerExe) -or -not (Test-Path $HdAdbExe)) {
    Log-Zen "ERROR" "No se encontro BlueStacks 5 en '$BlueStacksDir'." "Red"
    Log-Zen "ERROR" "Asegurese de tener BlueStacks 5 instalado." "Yellow"
    exit 1
}

Log-Zen "OK" "BlueStacks 5 localizado: $PlayerExe" "Green"

# 2. ACTIVAR ADB EN BLUESTACKS SI ESTA DESHABILITADO
if (Test-Path $ConfFile) {
    $confText = [System.IO.File]::ReadAllText($ConfFile)
    if ($confText -match 'bst\.enable_adb_access="0"') {
        Log-Zen "CONFIG" "Habilitando ADB en bluestacks.conf..." "Yellow"
        $confText = $confText -replace 'bst\.enable_adb_access="0"', 'bst\.enable_adb_access="1"'
        [System.IO.File]::WriteAllText($ConfFile, $confText)
        Log-Zen "OK" "ADB habilitado en la configuracion de BlueStacks." "Green"
    }
}

# Obtener puerto ADB configurado para la instancia
$adbPort = 5555
if (Test-Path $ConfFile) {
    $pattern = 'bst\.instance\.' + [regex]::Escape($Instance) + '\.(status\.)?adb_port="(\d+)"'
    $match = Select-String -Path $ConfFile -Pattern $pattern | Select-Object -First 1
    if ($match -and $match.Matches.Count -gt 0) {
        $adbPort = [int]$match.Matches[0].Groups[2].Value
    }
}
Log-Zen "INFO" "Puerto ADB objetivo para '$Instance': $adbPort" "DarkGray"

# 3. VERIFICAR O GESTIONAR APK
$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$DefaultApk  = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"

if (-not $LaunchOnly) {
    if ([string]::IsNullOrWhiteSpace($ApkPath)) {
        $ApkPath = $DefaultApk
    }

    if (-not (Test-Path $ApkPath)) {
        Log-Zen "WARN" "APK debug no encontrado en: $ApkPath" "Yellow"

        $javaCmd = Get-Command java -ErrorAction SilentlyContinue
        if (-not $javaCmd) {
            if ($AutoInstallJdk) {
                Log-Zen "INSTALL" "Instalando Microsoft OpenJDK 17 mediante winget..." "Cyan"
                winget install --id Microsoft.OpenJDK.17 -e --accept-source-agreements --accept-package-agreements
            } else {
                Log-Zen "INFO" "Entorno JDK no detectado en PATH." "Yellow"
                Log-Zen "INFO" "Para compilar el APK:" "Yellow"
                Log-Zen "INFO" "  1. Abra el proyecto en Android Studio y presione Build -> Make Project" "DarkGray"
                Log-Zen "INFO" "  2. O instale JDK con: .\scripts\deploy_bluestacks.ps1 -AutoInstallJdk" "DarkGray"
            }
        }

        $gradlew = Join-Path $ProjectRoot "gradlew.bat"
        if (Test-Path $gradlew) {
            Log-Zen "BUILD" "Compilando con gradlew assembleDebug..." "Cyan"
            & $gradlew assembleDebug
        }

        if (-not (Test-Path $ApkPath)) {
            Log-Zen "INFO" "Continuando en modo arranque de emulador..." "Yellow"
            $LaunchOnly = $true
        }
    } else {
        Log-Zen "OK" "APK detectado: $ApkPath" "Green"
    }
}

# 4. ARRANCAR BLUESTACKS 5 SI NO ESTA ACTIVO
$running = Get-Process -Name "HD-Player" -ErrorAction SilentlyContinue
if (-not $running) {
    Log-Zen "START" "Iniciando BlueStacks 5 (Instancia: $Instance)..." "Cyan"
    Start-Process -FilePath $PlayerExe -ArgumentList "--instance", $Instance
} else {
    Log-Zen "OK" "BlueStacks 5 ya esta corriendo." "Green"
}

# 5. CONECTAR ADB Y ESPERAR A QUE ANDROID ESTE LISTO
Log-Zen "CONNECT" "Conectando al puente ADB (127.0.0.1:$adbPort)..." "Cyan"
$maxWaitSeconds = 60
$elapsed = 0
$connected = $false

while ($elapsed -lt $maxWaitSeconds) {
    & $HdAdbExe connect "127.0.0.1:$adbPort" 2>&1 | Out-Null
    $devicesOut = & $HdAdbExe devices
    if ($devicesOut -match "127\.0\.0\.1:$adbPort\s+device" -or $devicesOut -match "emulator-\d+\s+device") {
        $bootCompleted = & $HdAdbExe -s "127.0.0.1:$adbPort" shell getprop sys.boot_completed 2>&1
        if ($bootCompleted -and $bootCompleted.Trim() -eq "1") {
            $connected = $true
            break
        }
    }
    Start-Sleep -Seconds 2
    $elapsed += 2
    Write-Host -NoNewline "."
}
Write-Host ""

if (-not $connected) {
    Log-Zen "WARN" "BlueStacks aun esta cargando o requiere autorizar conexion ADB." "Yellow"
    Log-Zen "INFO" "Si aparece una ventana en BlueStacks de 'Permitir depuracion USB', marque 'Permitir siempre'." "Yellow"
} else {
    Log-Zen "OK" "Conexion ADB establecida con exito y sistema operativo activo." "Green"
}

# 6. INSTALAR Y EJECUTAR
if (-not $LaunchOnly -and (Test-Path $ApkPath) -and $connected) {
    Log-Zen "INSTALL" "Instalando $ApkPath en emulador..." "Cyan"
    $installResult = & $HdAdbExe -s "127.0.0.1:$adbPort" install -r "$ApkPath"
    Log-Zen "OK" "Instalacion: $installResult" "Green"

    Log-Zen "LAUNCH" "Iniciando Pipes (MainActivity)..." "Cyan"
    & $HdAdbExe -s "127.0.0.1:$adbPort" shell am start -n "com.pipes.game/com.pipes.game.ui.MainActivity"
    Log-Zen "OK" "Pipes iniciado en BlueStacks 5." "Green"
}

Log-Zen "PIPES" "================================================" "Cyan"
Log-Zen "PIPES" "              PIPELINE FINALIZADO               " "Green"
Log-Zen "PIPES" "================================================" "Cyan"
Write-Host ""
