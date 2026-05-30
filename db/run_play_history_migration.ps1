#Requires -Version 5.1
<#
  合并 play_history 重复行并添加 uk_play_history_user_game。
  默认连接 localhost:3306 / gamecenter / root（与 application.yml 一致）。
  用法（仓库根目录）: .\db\run_play_history_migration.ps1
  依赖：mysql 客户端在 PATH 中（或修改 -MysqlPath）。
#>

param(
  [string] $DbHost = "127.0.0.1",
  [int] $Port = 3306,
  [string] $Database = "gamecenter",
  [string] $User = "root",
  [string] $Password = "123456",
  [string] $MysqlPath = "mysql"
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$sqlFile = Join-Path $scriptDir "migration_play_history_unique.sql"

if (-not (Test-Path $sqlFile)) {
  Write-Error "SQL file not found: $sqlFile"
}

$checkSql = "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = '$Database' AND table_name = 'play_history' AND index_name = 'uk_play_history_user_game';"

Write-Host "Checking if index uk_play_history_user_game exists..."
$env:MYSQL_PWD = $Password
try {
  $out = & $MysqlPath -h $DbHost -P $Port -u $User -N -s -e $checkSql 2>&1
  if ($LASTEXITCODE -ne 0) {
    throw "mysql check failed: $out"
  }
  $line = ($out | Select-Object -First 1)
  if ($null -eq $line) { $line = "0" }
  $cnt = [int]($line.ToString().Trim())
} finally {
  Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
}

if ($cnt -gt 0) {
  Write-Host "Index uk_play_history_user_game already exists — skip migration."
  exit 0
}

Write-Host "Running migration from: $sqlFile"
$env:MYSQL_PWD = $Password
try {
  Get-Content -LiteralPath $sqlFile -Raw -Encoding UTF8 | & $MysqlPath -h $DbHost -P $Port -u $User $Database
  if ($LASTEXITCODE -ne 0) {
    throw "mysql migration exited with code $LASTEXITCODE"
  }
} finally {
  Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
}

Write-Host "Migration finished OK."
