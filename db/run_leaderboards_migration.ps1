#Requires -Version 5.1
# 合并 leaderboards 重复行并添加 uk_leaderboard_game_user_type
# 用法: .\db\run_leaderboards_migration.ps1

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
$sqlFile = Join-Path $scriptDir "migration_leaderboards_unique.sql"

if (-not (Test-Path $sqlFile)) {
  Write-Error "SQL file not found: $sqlFile"
}

$checkSql = "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = '$Database' AND table_name = 'leaderboards' AND index_name = 'uk_leaderboard_game_user_type';"

Write-Host "Checking uk_leaderboard_game_user_type..."
$env:MYSQL_PWD = $Password
try {
  $out = & $MysqlPath -h $DbHost -P $Port -u $User -N -s -e $checkSql 2>&1
  if ($LASTEXITCODE -ne 0) { throw "mysql check failed: $out" }
  $cnt = [int](($out | Select-Object -First 1).ToString().Trim())
} finally {
  Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
}

if ($cnt -gt 0) {
  Write-Host "Unique index already exists — skip."
  exit 0
}

Write-Host "Running: $sqlFile"
$env:MYSQL_PWD = $Password
try {
  Get-Content -LiteralPath $sqlFile -Raw -Encoding UTF8 | & $MysqlPath -h $DbHost -P $Port -u $User $Database
  if ($LASTEXITCODE -ne 0) { throw "migration failed: $LASTEXITCODE" }
} finally {
  Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
}

Write-Host "Done."
