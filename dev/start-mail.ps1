param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'common.ps1')
Import-YebEnvironment
if (-not $env:MAIL_USERNAME -or -not $env:MAIL_PASSWORD -or -not $env:RABBITMQ_PASSWORD -or -not $env:DB_PASSWORD) {
    throw '请先在 dev/.env.local 配置邮箱、RabbitMQ 和 MySQL 账号。'
}
$env:JAVA_HOME = 'C:\Program Files\Java\jdk1.8.0_202'
$maven = Get-YebMaven
$root = Split-Path -Parent $PSScriptRoot
if (-not $SkipBuild) {
    & $maven -B -ntp -f (Join-Path $root 'pom.xml') -pl yeb-mail -am install -DskipTests
    if ($LASTEXITCODE -ne 0) { throw '邮件服务构建失败。' }
}
& $maven -B -ntp -f (Join-Path $root 'yeb-mail\pom.xml') org.springframework.boot:spring-boot-maven-plugin:2.4.6:run
if ($LASTEXITCODE -ne 0) { throw '邮件服务启动失败。' }
