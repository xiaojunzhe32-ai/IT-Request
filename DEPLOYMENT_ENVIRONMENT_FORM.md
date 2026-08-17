# IT Request 系统部署环境填写表

> 用途：部署到新服务器前，把本表复制为 `DEPLOYMENT_ENVIRONMENT_FORM.filled-服务器名.md` 后填写。填写后的文件包含账号、密码、端口、服务器地址，不要提交到 Git。  
> 模板文件可以提交，填写后的文件已被 `.gitignore` 忽略。

## 1. 环境基本信息

| 项目 | 填写内容 |
|---|---|
| 环境名称 | 例如：本地 / 测试服务器 / 政府生产服务器 |
| 服务器 IP / 域名 |  |
| 操作系统版本 |  |
| 部署负责人 |  |
| 部署日期 |  |
| Git 仓库地址 |  |
| Git 分支 / Commit |  |
| 对外访问地址 | 例如：http://192.168.1.25:8090 或 https://xxx.gov |

## 2. 服务器账号信息

| 项目 | 填写内容 | 备注 |
|---|---|---|
| SSH 用户 |  | 不建议长期使用 root |
| SSH 端口 | 22 | 如改过请填写实际端口 |
| 是否有 sudo 权限 | 是 / 否 |  |
| 项目部署目录 | 例如：/opt/it-request | 不要和其他系统共用目录 |
| Docker 数据卷策略 | 本机 volume / 外部盘 / 其他 |  |

## 3. Docker 与端口规划

| 服务 | 容器名 | 宿主机端口 | 容器端口 | 是否对外开放 | 备注 |
|---|---|---:|---:|---|---|
| 前端 Nginx | itop-web |  | 80 | 是 | 用户访问入口 |
| 后端 API | itop-api |  | 8080 | 建议仅内网 | 前端通过 `/api` 代理 |
| PostgreSQL | itop-postgres |  | 5432 | 建议仅内网/临时开放 | Navicat 如需连接再开放 |

## 4. `.env` 必填配置

| 变量名 | 填写内容 | 示例 / 说明 |
|---|---|---|
| DB_PASSWORD |  | 必须强密码，不要使用 `itop123` |
| DB_PORT |  | 默认 `5432`；服务器已有数据库时避免冲突 |
| API_PORT |  | 默认 `8080`；如被占用需改 |
| WEB_PORT |  | 默认 `80`；如部署到 8090 填 `8090` |
| JWT_SECRET |  | 至少 64 位随机字符串，不能使用示例占位值 |
| JWT_EXPIRATION | 86400000 | 默认 24 小时 |
| CORS_ALLOWED_ORIGIN_PATTERNS |  | 生产建议填写实际域名，例如 `https://xxx.gov` |
| ITOP_SWAGGER_ENABLED | false | 生产环境建议 `false` |
| SPRING_PROFILES_ACTIVE | prod | 生产使用 `prod` |
| POSTGRES_DB | itop | 通常不用改 |
| POSTGRES_USER | itop | 通常不用改 |
| JAVA_OPTS |  | 根据服务器内存调整 |

## 5. 本地与服务器可用性检查

| 检查项 | 命令 / 地址 | 期望结果 | 实际结果 |
|---|---|---|---|
| Docker 服务可用 | `docker ps` | 能列出容器 |  |
| Compose 配置有效 | `docker compose config` | 无配置错误 |  |
| 前端健康检查 | `http://服务器:WEB_PORT/health` | `healthy` |  |
| 后端健康检查 | `http://服务器:API_PORT/api/actuator/health` | `UP` |  |
| 登录接口 | `POST /api/auth/login` | 200，返回 token |  |
| 数据库端口 | `服务器:DB_PORT` | 按策略开放/关闭 |  |
| 页面入口 | `http://服务器:WEB_PORT/login` | 登录页正常显示 |  |

## 6. 数据库同步记录

| 项目 | 填写内容 |
|---|---|
| 数据来源 | 本地 / 旧服务器 / 生产服务器 |
| 源数据库地址 |  |
| 目标数据库地址 |  |
| 是否覆盖目标数据 | 是 / 否 |
| 同步时间 |  |
| 同步负责人 |  |
| 同步后表数量 |  |
| 同步后 Flyway 最新版本 |  |
| 是否已验证登录 | 是 / 否 |

## 7. 部署执行记录

| 步骤 | 命令 / 操作 | 完成情况 | 问题记录 |
|---|---|---|---|
| 拉取代码 | `git pull` 或首次 `git clone` |  |  |
| 创建 `.env` | 从 `.env.example` 复制并填写 |  |  |
| 构建镜像 | `docker compose build` |  |  |
| 启动服务 | `docker compose up -d` |  |  |
| 查看容器 | `docker compose ps` |  |  |
| 查看日志 | `docker compose logs -f itop-api` |  |  |
| 验证健康检查 | 见第 5 节 |  |  |
| 验证核心流程 | 登录、创建 Request、分配、处理、关闭 |  |  |

## 8. 常见失败原因排查

| 现象 | 优先检查 |
|---|---|
| 登录接口 502 | `itop-api` 是否启动；`JWT_SECRET` 是否为空/占位；后端端口是否正常 |
| 登录 403 | 账号密码是否正确；用户是否 active；角色权限是否同步 |
| 前端页面旧版本 | 是否重建 `itop-web` 镜像；浏览器缓存；Nginx 是否使用新容器 |
| 数据库连接失败 | `DB_PASSWORD`、端口映射、PostgreSQL 容器健康状态 |
| Flyway 启动失败 | 数据库 schema 与代码 migration 是否一致 |
| Navicat 连不上 | 5432 是否映射；防火墙是否放行；是否只允许内网访问 |

## 9. 上线前安全确认

| 检查项 | 是否完成 | 备注 |
|---|---|---|
| `.env` 未提交 Git |  |  |
| `JWT_SECRET` 已换成强随机值 |  |  |
| `DB_PASSWORD` 已换成强密码 |  |  |
| 生产 Swagger 已关闭 |  | `ITOP_SWAGGER_ENABLED=false` |
| CORS 已收紧到实际域名 |  |  |
| Demo 账号是否删除/改密 |  |  |
| PostgreSQL 端口是否限制访问 |  |  |
| HTTPS 是否配置 |  | 政府服务器建议启用 |
| 备份与回滚方案已确认 |  |  |

## 10. 回滚信息

| 项目 | 填写内容 |
|---|---|
| 上一个可用镜像版本 |  |
| 上一个可用 Git Commit |  |
| 数据库备份位置 |  |
| 回滚命令 |  |
| 回滚负责人 |  |
