param(
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$frontend = Join-Path $root 'yeb-front'
$dist = Join-Path $frontend 'dist'
$backend = Join-Path $root 'yeb-server'

if (-not $SkipBuild) {
    $portableNode = Join-Path $env:TEMP 'yeb-node18\portable\nodejs\node.exe'
    if (Test-Path $portableNode) {
        $node = $portableNode
    } else {
        $node = (Get-Command node -ErrorAction Stop).Source
    }
    if (-not (Test-Path (Join-Path $frontend 'node_modules\@vue\cli-service'))) {
        throw '前端依赖未安装：请先在 yeb-front 目录运行 npm ci --legacy-peer-deps。'
    }
    Push-Location $frontend
    try {
        & $node --openssl-legacy-provider '.\node_modules\@vue\cli-service\bin\vue-cli-service.js' build
        if ($LASTEXITCODE -ne 0) { throw '前端构建失败。' }
    } finally {
        Pop-Location
    }
}

if (-not (Test-Path (Join-Path $dist 'index.html'))) {
    throw '找不到 yeb-front\dist\index.html，请先构建前端。'
}

$localEnvironment = Join-Path $PSScriptRoot '.env.local'
if (Test-Path -LiteralPath $localEnvironment) {
    foreach ($line in Get-Content -LiteralPath $localEnvironment) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith('#')) {
            continue
        }
        $parts = $line -split '=', 2
        if ($parts.Count -ne 2 -or $parts[0] -notin @('REDIS_HOST', 'REDIS_PORT', 'REDIS_PASSWORD')) {
            throw 'dev/.env.local 仅支持 REDIS_HOST、REDIS_PORT、REDIS_PASSWORD，格式为 NAME=value。'
        }
        if ($null -eq [Environment]::GetEnvironmentVariable($parts[0], 'Process')) {
            [Environment]::SetEnvironmentVariable($parts[0], $parts[1], 'Process')
        }
    }
}

$env:JAVA_HOME = 'C:\Program Files\Java\jdk1.8.0_202'
$env:SERVER_PORT = '8080'
$env:SPRING_WEB_RESOURCES_STATIC_LOCATIONS = 'file:///' + ($dist -replace '\\', '/') + '/'
$maven = 'C:\Program Files\JetBrains\IntelliJ IDEA 2022.2.1\plugins\maven\lib\maven3\bin\mvn.cmd'
if (-not (Test-Path -LiteralPath $maven)) {
    $installedMaven = Get-Command mvn -ErrorAction SilentlyContinue
    if ($installedMaven) {
        $maven = $installedMaven.Source
    } else {
        $mavenCache = Join-Path $env:USERPROFILE '.m2\wrapper\dists'
        $cachedMaven = if (Test-Path -LiteralPath $mavenCache) {
            Get-ChildItem -LiteralPath $mavenCache -Filter mvn.cmd -File -Recurse |
                Sort-Object LastWriteTime -Descending |
                Select-Object -First 1
        }
        if (-not $cachedMaven) {
            throw '找不到 Maven：请安装 Maven 并加入 PATH，或先运行 Maven Wrapper。'
        }
        $maven = $cachedMaven.FullName
    }
}

Push-Location $backend
try {
    & $maven -B -ntp clean org.springframework.boot:spring-boot-maven-plugin:2.4.6:run
} finally {
    Pop-Location
}
