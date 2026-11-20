$ErrorActionPreference = 'Stop'
$projectPath = Split-Path -Parent $PSScriptRoot
$envPath = Join-Path $projectPath '.env'
if (Test-Path -LiteralPath $envPath) { throw '.env already exists. It was not changed.' }
$secretBytes = New-Object byte[] 32
$passwordBytes = New-Object byte[] 32
$generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$generator.GetBytes($secretBytes)
$generator.GetBytes($passwordBytes)
$generator.Dispose()
$jwtSecret = [Convert]::ToBase64String($secretBytes)
$dbPassword = [Convert]::ToBase64String($passwordBytes)
$lines = @('DB_URL=jdbc:postgresql://localhost:5433/habit_tracker', 'DB_USERNAME=habit_app', "DB_PASSWORD=$dbPassword", "JWT_SECRET=$jwtSecret")
[System.IO.File]::WriteAllLines($envPath, $lines, (New-Object System.Text.UTF8Encoding($false)))
Write-Output '.env created. Secret values are not displayed.'
