param(
    [switch]$Rebuild
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
$composeFile = Join-Path $root '..\docker-compose.yml'

if ($Rebuild) {
    docker-compose -f $composeFile up -d --build
} else {
    docker-compose -f $composeFile up -d
}

Write-Host "Compose started. Use 'docker-compose -f $composeFile logs -f backend' to watch backend logs."