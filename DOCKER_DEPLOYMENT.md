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

## 📞 技术支持

如遇问题，请查看：

1. 容器日志：`./deploy.sh logs`
2. 健康检查：访问 `/actuator/health`
3. 数据库日志：`./deploy.sh logs postgres`

---

**🎉 享受容器化的 CMDB 平台！**