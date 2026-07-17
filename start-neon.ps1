$ErrorActionPreference = "Stop"

function Invoke-CheckedCommand {
    param(
        [Parameter(Mandatory = $true)]
        [scriptblock]$Command,

        [Parameter(Mandatory = $true)]
        [string]$FailureMessage,

        [switch]$ShowPostgresLogsOnFailure
    )

    & $Command

    if ($LASTEXITCODE -ne 0) {
        if ($ShowPostgresLogsOnFailure) {
            Write-Host "PostgreSQL logs:"
            docker compose logs postgres
        }

        throw $FailureMessage
    }
}

function Import-DotEnv {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        throw "Missing .env file at $Path"
    }

    foreach ($rawLine in Get-Content -LiteralPath $Path) {
        $line = $rawLine.Trim()

        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith("#")) {
            continue
        }

        $separatorIndex = $line.IndexOf("=")
        if ($separatorIndex -le 0) {
            continue
        }

        $name = $line.Substring(0, $separatorIndex).Trim()
        $value = $line.Substring($separatorIndex + 1).Trim()

        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }

        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

function Test-RequiredEnvironment {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Names
    )

    foreach ($name in $Names) {
        $value = [Environment]::GetEnvironmentVariable($name, "Process")
        if ([string]::IsNullOrWhiteSpace($value) -or $value -like "YOUR_*") {
            throw "Missing required environment variable: $name. Set it in .env first."
        }
    }
}

$envPath = Join-Path -Path $PSScriptRoot -ChildPath ".env"
Import-DotEnv -Path $envPath

Test-RequiredEnvironment -Names @(
    "LOCAL_POSTGRES_DB",
    "LOCAL_POSTGRES_USERNAME",
    "LOCAL_POSTGRES_PASSWORD"
)

Write-Host "Starting local PostgreSQL container..."
Invoke-CheckedCommand `
    -Command { docker compose up -d postgres } `
    -FailureMessage "Failed to start local PostgreSQL with Docker Compose." `
    -ShowPostgresLogsOnFailure

Write-Host "Waiting for ahadith-postgres to become healthy..."
$isHealthy = $false
for ($attempt = 1; $attempt -le 30; $attempt++) {
    $health = docker inspect -f "{{.State.Health.Status}}" ahadith-postgres 2>$null

    if ($LASTEXITCODE -ne 0) {
        Write-Host "PostgreSQL container is not inspectable yet. Attempt $attempt of 30."
    } elseif ($health -eq "healthy") {
        $isHealthy = $true
        break
    } else {
        Write-Host "PostgreSQL health is '$health'. Attempt $attempt of 30."
    }

    Start-Sleep -Seconds 2
}

if (-not $isHealthy) {
    Write-Host "PostgreSQL logs:"
    docker compose logs postgres
    throw "PostgreSQL container ahadith-postgres did not become healthy after 30 attempts."
}

Write-Host "Testing local PostgreSQL connection..."
Invoke-CheckedCommand `
    -Command { docker compose exec -T postgres psql -U $env:LOCAL_POSTGRES_USERNAME -d $env:LOCAL_POSTGRES_DB -c "SELECT current_database(), current_user, 1 AS test;" } `
    -FailureMessage "Local PostgreSQL connection test failed." `
    -ShowPostgresLogsOnFailure

Test-RequiredEnvironment -Names @(
    "NEON_DB_URL",
    "NEON_DB_USERNAME",
    "NEON_DB_PASSWORD"
)

$env:SPRING_PROFILES_ACTIVE = "dev"
$env:SPRING_DATASOURCE_URL = $env:NEON_DB_URL
$env:SPRING_DATASOURCE_USERNAME = $env:NEON_DB_USERNAME
$env:SPRING_DATASOURCE_PASSWORD = $env:NEON_DB_PASSWORD

Write-Host "Starting Spring Boot with Neon datasource..."
Invoke-CheckedCommand `
    -Command { .\mvnw.cmd spring-boot:run } `
    -FailureMessage "Spring Boot application failed to start."
