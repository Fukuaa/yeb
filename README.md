# 云 E 办：本地启动

本仓库包含 Spring Boot 后端（`yeb-server`）、Vue 前端（`yeb-front`）、邮件模块和代码生成模块。

## 准备

1. 启动 Ubuntu 虚拟机。MySQL 8.0.30 已迁移到 Docker，固定地址为 `192.168.240.10:3306`，原有 `yeb` 数据已恢复；已有迁移数据时无需重新导入。新建环境才需要创建 `yeb` 数据库并导入 [`database/yeb.sql`](database/yeb.sql)。数据库连接参数在 `yeb-server/src/main/resources/config/application.yml`，可用 `DB_URL`、`DB_USER` 环境变量覆盖。
2. Redis 已迁移到 Ubuntu Docker，固定地址为 `192.168.240.10:6379`，启用了密码认证、AOF 持久化和自动启动。无需再运行本机 `redis-server.exe`。本机启动脚本自动读取不提交到 Git 的 `dev/.env.local`；该文件已经配置好。新电脑可复制 `dev/.env.local.example` 为 `dev/.env.local`，填入 Ubuntu `~/database-migration/.env` 中的 `REDIS_PASSWORD`。已有进程环境变量优先于文件设置。通过 IDE 启动后端或邮件模块时，也需要设置 `REDIS_PASSWORD` 环境变量，可选设置 `REDIS_HOST`、`REDIS_PORT`。

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

修改前端代码后，停止脚本并重新运行即可更新页面。若只修改后端且已构建前端，可运行 `.\dev\start-local.ps1 -SkipBuild`。聊天服务随主服务启动；邮件发送需要在另一个 PowerShell 窗口运行 `./dev/start-mail.ps1`。

Ubuntu 使用静态 IP `192.168.240.10`，数据库容器配置了固定端口、持久化数据卷和 `unless-stopped` 重启策略。虚拟机启动并运行 Docker 后，数据库会自动启动；虚拟机关机时本机应用无法访问数据库。RabbitMQ 也使用这台虚拟机，默认连接地址为 `192.168.240.10:5672`；Redis 使用同一台虚拟机的 `6379` 端口。Redis 快照与校验报告保存在 Ubuntu `~/database-migration/backups`，原始 Windows 文件另存于 `C:\ProgramData\DatabaseMigrationBackup\20261002`。

当前教材前端已有员工基本资料、系统基础信息、操作员、工资账套、员工账套和个人信息页面。菜单中的人事管理、统计管理等 16 个页面仍是原仓库的占位组件；对应后端控制器也未实现业务接口，需要另行开发。


## 聊天和邮件

已有数据库升级时，执行一次 `database/chat.sql`，增加聊天记录表；脚本可重复执行，不会覆盖现有数据。当前 Ubuntu 的 `yeb` 数据库已完成此升级。新环境导入 `database/yeb.sql` 后也需要执行该脚本。

聊天入口在页面右上角。支持联系人搜索、实时消息、服务器保存的历史记录、加载更早消息、离线未读和已读更新。连接中断后自动重连，退出登录时断开连接并清理当前账号的聊天状态。正文最多 2000 字，发送者由后端登录身份确定。

邮件使用 Ubuntu RabbitMQ（`192.168.240.10:5672`）和独立的 `yeb-mail` 进程。在两个 PowerShell 窗口分别运行：

```powershell
cd C:\Users\19365\yeb
.\dev\start-local.ps1
```

```powershell
cd C:\Users\19365\yeb
.\dev\start-mail.ps1
```

本机 `dev/.env.local` 已配置从旧 Claude SMTP MCP 恢复的邮箱设置及数据库、RabbitMQ、Redis 连接信息；该文件不会提交到 Git。新电脑请按 `dev/.env.local.example` 配置，IDE 启动时也需要传入这些环境变量。QQ 邮箱使用 SMTP 授权码，示例采用 587 端口和 STARTTLS。启动脚本不依赖 Claude 或 MCP 进程。

添加员工会在同一数据库事务中生成欢迎邮件任务，后台每 10 秒检查待发送任务。邮件模块实际提交给 SMTP 成功后才标记为“已发送”；SMTP 接受不代表最终进入收件箱。失败任务自动尝试最多 3 次，间隔约 1 分钟。管理员可点击右上角邮件入口查看记录、按员工发送欢迎邮件或手动重试失败任务。队列重复投递已完成任务时不会再次发送；SMTP 成功后进程恰好在状态写回前崩溃，恢复后仍可能重复发送。

验证命令（需要 Java 8 和 Maven）：

```powershell
mvn -pl yeb-server,yeb-mail -am test
```

聊天和邮件包含 13 项后端测试；完整链路另外使用独立 MySQL 测试库、RabbitMQ 测试虚拟主机及本地 SMTP 接收器验证，不给真实员工发送测试邮件。
