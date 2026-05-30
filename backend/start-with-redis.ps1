# 开发用：在启动 Spring Boot 前先确保本机 Redis 与前端开发服务已启动
# 用法：
#   .\start-with-redis.ps1                      # 起 Redis + 用户端 + 管理端 + mvn spring-boot:run
#   .\start-with-redis.ps1 -RedisOnly           # 仅起 Redis 后退出
#   .\start-with-redis.ps1 -ClientFrontendOnly  # 仅起用户端 (10109) 后退出
#   .\start-with-redis.ps1 -AdminFrontendOnly   # 仅起管理端 (10110) 后退出
#   .\start-with-redis.ps1 -FrontendOnly        # 同 -ClientFrontendOnly
#   .\start-with-redis.ps1 -SkipRedis           # 不处理 Redis
#   .\start-with-redis.ps1 -SkipFrontend        # 不处理任何前端
#   .\start-with-redis.ps1 -SkipAdminFrontend   # 不启动管理端
#   .\start-with-redis.ps1 -SkipClientFrontend  # 不启动用户端
# 目录默认 D:\Redis，可设置环境变量 GAMECENTER_REDIS_HOME 覆盖
param(
    [switch] $RedisOnly,
    [switch] $FrontendOnly,
    [switch] $ClientFrontendOnly,
    [switch] $AdminFrontendOnly,
    [switch] $SkipRedis,
    [switch] $SkipFrontend,
    [switch] $SkipAdminFrontend,
    [switch] $SkipClientFrontend
)

$ErrorActionPreference = 'Stop'

$redisHome = if ($env:GAMECENTER_REDIS_HOME) { $env:GAMECENTER_REDIS_HOME } else { 'D:\Redis' }
$port = 6379
if ($env:SPRING_DATA_REDIS_PORT) {
    $p = 0
    if ([int]::TryParse($env:SPRING_DATA_REDIS_PORT, [ref]$p)) { $port = $p }
}
$serverExe = Join-Path $redisHome 'redis-server.exe'
$projectRoot = Split-Path -Parent $PSScriptRoot
$clientFrontendDir = Join-Path $projectRoot 'frontend'
$adminFrontendDir = Join-Path $projectRoot 'admin-frontend'
$clientPort = 10109
$adminPort = 10110
if ($env:GAMECENTER_FRONTEND_PORT) {
    $fp = 0
    if ([int]::TryParse($env:GAMECENTER_FRONTEND_PORT, [ref]$fp)) { $clientPort = $fp }
}
if ($env:GAMECENTER_ADMIN_FRONTEND_PORT) {
    $ap = 0
    if ([int]::TryParse($env:GAMECENTER_ADMIN_FRONTEND_PORT, [ref]$ap)) { $adminPort = $ap }
}

function Test-LocalPortOpen {
    param([int] $LocalPort)
    $c = $null
    try {
        $c = New-Object System.Net.Sockets.TcpClient
        $c.Connect('127.0.0.1', $LocalPort)
        $true
    } catch {
        $false
    } finally {
        if ($null -ne $c) { $c.Close() }
    }
}

function Start-DevServerIfNeeded {
    param(
        [string] $FrontendDir,
        [int] $LocalPort,
        [string] $Label
    )

    if (-not (Test-Path -LiteralPath $FrontendDir)) {
        Write-Warning "未找到前端目录: $FrontendDir"
        return
    }

    if (Test-LocalPortOpen -LocalPort $LocalPort) {
        Write-Host "$Label 已在 127.0.0.1:$LocalPort 可连接，跳过启动。"
        return
    }

    $packageJson = Join-Path $FrontendDir 'package.json'
    if (-not (Test-Path -LiteralPath $packageJson)) {
        Write-Warning "未找到 package.json: $packageJson"
        return
    }

    $safeDir = $FrontendDir.Replace("'", "''")
    $command = "Set-Location -LiteralPath '$safeDir'; npm run dev"
    Write-Host "正在启动 $Label : npm run dev (端口 $LocalPort) ..."
    Start-Process -FilePath 'powershell' `
        -ArgumentList @('-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', $command) `
        -WorkingDirectory $FrontendDir `
        -WindowStyle Normal

    Start-Sleep -Seconds 2
    if (Test-LocalPortOpen -LocalPort $LocalPort) {
        Write-Host "$Label 已就绪。"
    } else {
        Write-Warning "$Label 尚未检测到 127.0.0.1:$LocalPort，可能仍在编译。"
    }
}

if (-not $SkipRedis) {
    if (-not (Test-Path -LiteralPath $serverExe)) {
        Write-Warning "未找到 Redis 可执行文件: $serverExe"
        Write-Warning "请设置 GAMECENTER_REDIS_HOME 指向本机 Redis 目录，或手动启动 redis-server。"
        if ($RedisOnly) { exit 1 }
    } else {
        if (Test-LocalPortOpen -LocalPort $port) {
            Write-Host "Redis 已在 127.0.0.1:$port 可连接，跳过启动。"
        } else {
            $conf = Join-Path $redisHome 'redis.conf'
            Write-Host "正在启动 Redis: $serverExe (端口 $port) ..."
            if (Test-Path -LiteralPath $conf) {
                Start-Process -FilePath $serverExe -ArgumentList $conf -WorkingDirectory $redisHome -WindowStyle Minimized
            } else {
                Start-Process -FilePath $serverExe -WorkingDirectory $redisHome -WindowStyle Minimized
            }
            Start-Sleep -Seconds 1
            if (-not (Test-LocalPortOpen -LocalPort $port)) {
                Write-Warning "Redis 启动后仍无法连接 127.0.0.1:$port，请检查端口或 conf。"
                if ($RedisOnly) { exit 1 }
            } else {
                Write-Host "Redis 已就绪。"
            }
        }
    }
}

if ($RedisOnly) { exit 0 }

$startClient = -not $SkipFrontend -and -not $SkipClientFrontend -and -not $AdminFrontendOnly
$startAdmin = -not $SkipFrontend -and -not $SkipAdminFrontend -and -not ($FrontendOnly -or $ClientFrontendOnly)

if ($FrontendOnly -or $ClientFrontendOnly) {
    $startClient = -not $SkipFrontend -and -not $SkipClientFrontend
    $startAdmin = $false
}

if ($AdminFrontendOnly) {
    $startClient = $false
    $startAdmin = -not $SkipFrontend
}

if ($startClient) {
    Start-DevServerIfNeeded -FrontendDir $clientFrontendDir -LocalPort $clientPort -Label '用户端前端'
}

if ($startAdmin) {
    Start-DevServerIfNeeded -FrontendDir $adminFrontendDir -LocalPort $adminPort -Label '管理端前端'
}

if ($FrontendOnly -or $ClientFrontendOnly -or $AdminFrontendOnly) { exit 0 }

Set-Location -LiteralPath $PSScriptRoot
mvn spring-boot:run
exit $LASTEXITCODE
