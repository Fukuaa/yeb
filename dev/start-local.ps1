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

. (Join-Path $PSScriptRoot 'common.ps1')
Import-YebEnvironment

$env:JAVA_HOME = 'C:\Program Files\Java\jdk1.8.0_202'
$env:SERVER_PORT = '8080'
$env:SPRING_WEB_RESOURCES_STATIC_LOCATIONS = 'file:///' + ($dist -replace '\\', '/') + '/'
$maven = Get-YebMaven

Push-Location $backend
try {
    & $maven -B -ntp clean org.springframework.boot:spring-boot-maven-plugin:2.4.6:run
} finally {
    Pop-Location
}
