function Import-YebEnvironment {
    $localEnvironment = Join-Path $PSScriptRoot '.env.local'
    $allowed = @('DB_URL','DB_USER','DB_PASSWORD','REDIS_HOST','REDIS_PORT','REDIS_PASSWORD',
        'RABBITMQ_HOST','RABBITMQ_PORT','RABBITMQ_USER','RABBITMQ_PASSWORD','RABBITMQ_VHOST',
        'MAIL_HOST','MAIL_PORT','MAIL_USERNAME','MAIL_PASSWORD','MAIL_FROM','MAIL_AUTH','MAIL_STARTTLS','MAIL_SSL',
        'MAIL_QUEUE','MAIL_EXCHANGE','MAIL_ROUTING_KEY','MAIL_DISPATCH_ENABLED','WEBSOCKET_ALLOWED_ORIGINS')
    if (Test-Path -LiteralPath $localEnvironment) {
        foreach ($line in Get-Content -LiteralPath $localEnvironment) {
            if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith('#')) { continue }
            $parts = $line -split '=', 2
            if ($parts.Count -ne 2 -or $parts[0] -notin $allowed) {
                throw 'dev/.env.local 包含不支持的变量，或格式不是 NAME=value。'
            }
            if ($null -eq [Environment]::GetEnvironmentVariable($parts[0], 'Process')) {
                [Environment]::SetEnvironmentVariable($parts[0], $parts[1], 'Process')
            }
        }
    }
}

function Get-YebMaven {
    $installed = Get-Command mvn -ErrorAction SilentlyContinue
    if ($installed) { return $installed.Source }
    $cache = Join-Path $env:USERPROFILE '.m2\wrapper\dists'
    $cached = if (Test-Path -LiteralPath $cache) {
        Get-ChildItem -LiteralPath $cache -Filter mvn.cmd -File -Recurse |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1
    }
    if (-not $cached) { throw '找不到 Maven：请安装 Maven 并加入 PATH，或先运行 Maven Wrapper。' }
    return $cached.FullName
}
