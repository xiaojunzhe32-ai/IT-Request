# Docker 部署指南

## 🚀 快速开始

### 前置要求

- Docker Desktop（Windows/macOS）或 Docker Engine（Linux）
- Docker Compose
- 至少 4GB 可用内存
- 至少 10GB 可用磁盘空间

### 一键部署

#### Windows:
```bash
deploy.bat build
deploy.bat start
```

#### Linux/macOS:
```bash
chmod +x deploy.sh
./deploy.sh build
./deploy.sh start
```

访问：`http://localhost`

默认账号：`admin` / `admin123`

---

## 📦 服务架构

### 容器说明

| 服务名 | 容器名 | 端口 | 说明 |
|--------|--------|------|------|
| postgres | itop-postgres | 5432 | PostgreSQL 数据库 |
| itop-api | itop-api | 8080 | Spring Boot 后端 API |
| itop-web | itop-web | 80 | Vue 3 前端（Nginx） |

### 网络架构

```
[浏览器] --> [itop-web:80] --> [itop-api:8080] --> [postgres:5432]
              (Nginx)          (Spring Boot)       (PostgreSQL)
```

---

## ⚙️ 配置

### 环境变量

复制 `.env.example` 到 `.env` 并修改：

```bash
cp .env.example .env
```

关键配置：

```env
# 数据库密码（强烈建议修改）
DB_PASSWORD=your_secure_password_here

# JWT密钥（必须修改）
JWT_SECRET=your_long_random_secret_key_at_least_256_bits

# 端口映射
DB_PORT=5432
API_PORT=8080
WEB_PORT=80

# JVM内存配置
JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC
```

---

## 🛠️ 部署脚本命令

### 基础命令

```bash
# 构建镜像
./deploy.sh build

# 启动服务
./deploy.sh start

# 停止服务
./deploy.sh stop

# 重启服务
./deploy.sh restart

# 查看状态
./deploy.sh status

# 查看日志
./deploy.sh logs
./deploy.sh logs itop-api
./deploy.sh logs postgres
```

### 数据库管理

```bash
# 备份数据库
./deploy.sh backup-db

# 恢复数据库
./deploy.sh restore-db itop_backup_20240115_103045.sql

# 初始化数据库
./deploy.sh init-db
```

### 清理

```bash
# 清理所有容器、卷和镜像（危险操作！）
./deploy.sh clean
```

---

## 📊 监控与健康检查

### 健康检查端点

- **前端**: `http://localhost/health`
- **后端**: `http://localhost:8080/api/actuator/health`
- **数据库**: 内部健康检查

### 查看容器状态

```bash
docker-compose ps
```

### 查看资源使用

```bash
docker stats itop-postgres itop-api itop-web
```

---

## 🔧 高级配置

### 生产环境推荐配置

#### 1. 修改 `docker-compose.yml`

```yaml
services:
  postgres:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
  itop-api:
    environment:
      JAVA_OPTS: -Xms1g -Xmx2g -XX:+UseG1GC
    deploy:
      replicas: 2  # 多实例
```

#### 2. 启用 HTTPS

创建 `nginx-ssl.conf`:

```nginx
server {
    listen 443 ssl;
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    # ... 其他配置
}
```

#### 3. 数据持久化

所有数据已通过 Docker Volume 持久化：

```bash
# 查看卷
docker volume ls | grep itop

# 卷位置
# postgres_data: /var/lib/postgresql/data
```

---

## 🔐 安全建议

### 1. 修改默认密码

```bash
# 修改 .env 文件
DB_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 64)
```

### 2. 限制端口暴露

仅暴露必要端口，生产环境建议：

```yaml
# docker-compose.yml
services:
  postgres:
    ports: []  # 不暴露端口，仅内部访问
```

### 3. 使用 Docker Secrets

```yaml
services:
  postgres:
    secrets:
      - db_password

secrets:
  db_password:
    file: ./secrets/db_password.txt
```

---

## 🚨 故障排查

### 常见问题

#### 1. 端口被占用

```bash
# 检查端口占用
netstat -ano | findstr :80
netstat -ano | findstr :8080
netstat -ano | findstr :5432

# 修改 .env 中的端口
WEB_PORT=8081
API_PORT=8082
DB_PORT=5433
```

#### 2. 容器无法启动

```bash
# 查看详细日志
docker-compose logs itop-api

# 检查容器状态
docker inspect itop-api

# 重建容器
docker-compose down
docker-compose up -d --build
```

#### 3. 数据库连接失败

```bash
# 检查数据库状态
docker-compose exec postgres pg_isready -U itop

# 手动连接测试
docker-compose exec postgres psql -U itop -d itop
```

#### 4. 磁盘空间不足

```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的卷
docker volume prune

# 查看磁盘使用
docker system df
```

---

## 📈 扩展部署

### 水平扩展

```bash
# 扩展 API 服务到 3 个实例
docker-compose up -d --scale itop-api=3
```

### 使用外部数据库

修改 `docker-compose.yml`:

```yaml
services:
  itop-api:
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://external-db.example.com:5432/itop

# 移除 postgres 服务
```

### 集群部署（Swarm/K8s）

提供了 Kubernetes 配置示例，见 `k8s/` 目录（需自行创建）。

---

## 📝 维护操作

### 日常维护

```bash
# 1. 备份数据库
./deploy.sh backup-db

# 2. 查看日志
./deploy.sh logs --tail=1000 > logs_$(date +%Y%m%d).log

# 3. 更新镜像
docker-compose pull
docker-compose up -d

# 4. 清理日志
docker-compose exec postgres truncate table audit_log
```

### 版本升级

```bash
# 1. 备份数据
./deploy.sh backup-db

# 2. 拉取新代码
git pull

# 3. 重新构建
./deploy.sh build

# 4. 重启服务
./deploy.sh restart
```

---

## 🌐 生产环境检查清单

- [ ] 修改所有默认密码（`.env` 文件）
- [ ] 启用 HTTPS（配置 SSL 证书）
- [ ] 配置防火墙规则
- [ ] 设置定期数据库备份
- [ ] 配置日志轮转
- [ ] 启用监控告警（Prometheus/Grafana）
- [ ] 限制资源使用（CPU/内存）
- [ ] 配置日志聚合（ELK Stack）
- [ ] 定期安全审计
- [ ] 灾难恢复演练

---

## 192.168.1.25 当前构建记录（不要重复踩坑）

### 已验证的后端更新路线

```bash
# 本地打包完整后端构建上下文，不要只传旧镜像 tar
7z a -ttar itop-src.tar pom.xml itop-common/pom.xml itop-common/src itop-core/pom.xml itop-core/src itop-cmdb/pom.xml itop-cmdb/src itop-api/pom.xml itop-api/src itop-api/Dockerfile itop-web/pom.xml -xr!target -xr!node_modules -xr!dist

# 服务器使用 mh + sudo；root SSH 登录失败过，不要再试 root
cd /home/mh/itop-java
docker compose build --no-cache itop-api
docker compose up -d --no-deps itop-api
```

必须保留当前 `itop-api/Dockerfile` 的服务器友好改动：Maven 使用阿里云 mirror、不要 `apk add`、healthcheck 使用 Alpine 自带 `wget`。服务器 `/home/mh/itop-java` 可能没有完整源码，构建前必须上传并解压最新 `itop-src.tar`。

### 已验证失败或不推荐路线

- 不要依赖旧的本地 `itop-api.tar` / `itop-images.tar`，容易部署到过期镜像。
- 本机 Docker Desktop daemon 未稳定前，不要走本机构建。
- 服务器上不要用 `docker-compose`，该机可用的是 `docker compose`。
- 不要让 Dockerfile 在服务器构建时访问 Alpine `apk add` 或 Maven Central 直连，之前会 TLS 失败。
- 不要用 `docker compose up --force-recreate itop-api` 在外部网络被孤儿容器占用时强制重建；若只修健康检查，可最小化重建 API 容器配置。

### 当前健康检查结论

`docker-compose.yml` 会覆盖镜像里的 healthcheck。后端容器内没有 `curl`，所以 compose 中必须使用：

```yaml
healthcheck:
  test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/api/actuator/health"]
```

前端容器也不要使用 `curl` 或 `localhost` 健康检查；服务器已验证可用命令是：

```yaml
healthcheck:
  test: ["CMD", "wget", "-q", "-O", "/dev/null", "http://127.0.0.1:80/health"]
```

### 已验证的前端更新路线

只更新左侧菜单/页面等前端内容时，不要重建 API，不要碰数据库。服务器 compose 在存在活动网络端点时可能会尝试删除 `itop-java_itop-network` 并失败，因此已验证的最小路线是：

```bash
# 本地只打包前端构建上下文
7z a -ttar itop-web-src.tar docker-compose.yml itop-web/Dockerfile itop-web/nginx.conf itop-web/src/main/frontend -xr!node_modules -xr!dist

# 服务器只构建前端镜像
cd /home/mh/itop-java
docker compose build --no-cache itop-web

# 若 docker compose up -d --no-deps itop-web 因网络删除失败，手动只替换 web 容器
docker rm -f itop-web
docker run -d --name itop-web --restart unless-stopped --network itop-java_itop-network -p 8090:80 \
  --health-cmd='wget -q -O /dev/null http://127.0.0.1:80/health || exit 1' \
  --health-interval=30s --health-timeout=5s --health-retries=3 --health-start-period=10s \
  itop-web:latest
```

2026-08-13 已验证服务器状态：`itop-api` healthy，`itop-web` healthy，页面 `http://localhost:8090/` 返回 200。构建产物中未再检出独立菜单字符串 `Organizations / Roles / Permissions / Routing Rules`。

2026-08-13 已验证服务器后端状态：`itop-api Up ... (healthy)`，接口 `http://localhost:8080/api/actuator/health` 返回 `{"status":"UP"}`。

---

## 📞 技术支持

如遇问题，请查看：

1. 容器日志：`./deploy.sh logs`
2. 健康检查：访问 `/actuator/health`
3. 数据库日志：`./deploy.sh logs postgres`

---

**🎉 享受容器化的 CMDB 平台！**