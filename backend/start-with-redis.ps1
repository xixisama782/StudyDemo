# 开发用：在启动 Spring Boot 前先确保本机 Redis 与前端开发服务已启动
# 用法：
#   .\start-with-redis.ps1                 # 起 Redis + 前端 + mvn spring-boot:run
#   .\start-with-redis.ps1 -RedisOnly      # 仅起 Redis 后退出（供 VS Code 等前置任务使用）
#   .\start-with-redis.ps1 -FrontendOnly   # 仅起前端后退出
#   .\start-with-redis.ps1 -SkipRedis      # 不处理 Redis
#   .\start-with-redis.ps1 -SkipFrontend   # 不处理前端
# 目录默认 D:\Redis，可设置环境变量 GAMECENTER_REDIS_HOME 覆盖
param(
    [switch] $RedisOnly,
    [switch] $FrontendOnly,
    [switch] $SkipRedis,
    [switch] $SkipFrontend
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
$frontendDir = Join-Path $projectRoot 'frontend'
$frontendPort = 10109
if ($env:GAMECENTER_FRONTEND_PORT) {
    $fp = 0
    if ([int]::TryParse($env:GAMECENTER_FRONTEND_PORT, [ref]$fp)) { $frontendPort = $fp }
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

function Start-FrontendIfNeeded {
    if (-not (Test-Path -LiteralPath $frontendDir)) {
        Write-Warning "未找到前端目录: $frontendDir"
        return
    }

    if (Test-LocalPortOpen -LocalPort $frontendPort) {
        Write-Host "前端开发服务已在 127.0.0.1:$frontendPort 可连接，跳过启动。"
        return
    }

    $packageJson = Join-Path $frontendDir 'package.json'
    if (-not (Test-Path -LiteralPath $packageJson)) {
        Write-Warning "未找到前端 package.json: $packageJson"
        return
    }

    $safeFrontendDir = $frontendDir.Replace("'", "''")
    $frontendCommand = "Set-Location -LiteralPath '$safeFrontendDir'; npm run dev"
    Write-Host "正在启动前端开发服务: npm run dev (端口 $frontendPort) ..."
    Start-Process -FilePath 'powershell' `
        -ArgumentList @('-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', $frontendCommand) `
        -WorkingDirectory $frontendDir `
        -WindowStyle Normal

    Start-Sleep -Seconds 2
    if (Test-LocalPortOpen -LocalPort $frontendPort) {
        Write-Host "前端开发服务已就绪。"
    } else {
        Write-Warning "前端服务尚未检测到 127.0.0.1:$frontendPort，可能仍在编译或端口已变化。"
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

if (-not $SkipFrontend) {
    Start-FrontendIfNeeded
}

if ($FrontendOnly) { exit 0 }

Set-Location -LiteralPath $PSScriptRoot
mvn spring-boot:run
exit $LASTEXITCODE
