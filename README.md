# 云 E 办：本地开发启动

本仓库包含 Spring Boot 后端（`yeb-server`）、Vue 前端（`yeb-front`）、邮件模块（`yeb-mail`）和代码生成模块（`yeb-generator`）。

## 准备环境

- JDK 8、Maven 3、MySQL 8、Redis 5、Node.js 22。
- 在 MySQL 中创建 UTF-8 数据库 `yeb`，导入 [`database/yeb.sql`](database/yeb.sql)。后端数据库连接参数在 `yeb-server/src/main/resources/config/application.yml`。
- 启动 Redis，监听本机 6379 且不设置密码。例如在 PowerShell 中运行：

```powershell
& 'I:\原F盘\Redis-x64-5.0.14\redis-server.exe' .\dev\redis.conf
```

## 启动后端

在仓库根目录的 PowerShell 中运行：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk1.8.0_202'
$env:SPRING_REDIS_HOST = '127.0.0.1'
cd yeb-server
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2022.2.1\plugins\maven\lib\maven3\bin\mvn.cmd' clean org.springframework.boot:spring-boot-maven-plugin:2.4.6:run
```

后端监听 `http://localhost:8081`。使用 `clean` 是为了从源码重新编译，避免仓库中旧的 `target` 产物影响开发运行。

## 启动前端

另开一个 PowerShell 终端，在仓库根目录运行：

```powershell
cd yeb-front
npm ci --legacy-peer-deps
npm run serve
```

访问 `http://localhost:8080`。示例账号为 `admin` / `123`，登录还需填写页面显示的验证码。

邮件模块依赖 RabbitMQ 和邮件服务配置；以上步骤先启动可登录的前后端管理系统。
