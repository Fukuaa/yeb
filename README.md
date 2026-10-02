# 云 E 办：本地启动

本仓库包含 Spring Boot 后端（`yeb-server`）、Vue 前端（`yeb-front`）、邮件模块和代码生成模块。

## 准备

1. 启动 Ubuntu 虚拟机。MySQL 8.0.30 已迁移到 Docker，固定地址为 `192.168.240.10:3306`，原有 `yeb` 数据已恢复；已有迁移数据时无需重新导入。新建环境才需要创建 `yeb` 数据库并导入 [`database/yeb.sql`](database/yeb.sql)。数据库连接参数在 `yeb-server/src/main/resources/config/application.yml`，可用 `DB_URL`、`DB_USER` 环境变量覆盖。
2. 启动 Redis 5（本机 6379，无密码）：

   ```powershell
   & 'I:\原F盘\Redis-x64-5.0.14\redis-server.exe' .\dev\redis.conf
   ```

3. 首次运行时，在 `yeb-front` 目录安装前端依赖：

   ```powershell
   cd yeb-front
   npm ci --legacy-peer-deps
   cd ..
   ```

## 启动

在仓库根目录运行：

```powershell
.\dev\start-local.ps1
```

脚本优先使用临时目录中的 Node 18（`%TEMP%\yeb-node18\portable\nodejs\node.exe`）构建 Vue，然后通过 Spring Boot 的开发运行命令在 **8080** 端口同时提供页面和 API。它不会修改系统 Node 安装。访问 [http://localhost:8080/#/](http://localhost:8080/#/)；示例账号为 `admin` / `123`，登录时填写页面上的验证码。

修改前端代码后，停止脚本并重新运行即可更新页面。若只修改后端且已构建前端，可运行 `.\dev\start-local.ps1 -SkipBuild`。邮件功能另需 RabbitMQ 和邮件服务配置。

Ubuntu 使用静态 IP `192.168.240.10`，数据库容器配置了固定端口、持久化数据卷和 `unless-stopped` 重启策略。虚拟机启动并运行 Docker 后，数据库会自动启动；虚拟机关机时本机应用无法访问数据库。RabbitMQ 也使用这台虚拟机，默认连接地址已同步为 `192.168.240.10:5672`。Redis 仍由本机启动脚本连接 `127.0.0.1:6379`。

当前教材前端已有员工基本资料、系统基础信息、操作员、工资账套、员工账套和个人信息页面。菜单中的人事管理、统计管理等 16 个页面仍是原仓库的占位组件；对应后端控制器也未实现业务接口，需要另行开发。
