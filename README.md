# iTop Java - Modern CMDB Platform

基于 iTop 核心概念，使用现代技术栈重构的轻量级 CMDB 平台。

## 🎯 项目概况

这是一个从 PHP + MySQL 技术栈迁移到 Java + PostgreSQL 的完整重写版本，保留核心 CMDB 功能，采用现代架构设计。

### 技术栈

**后端**：
- ☕ Java 21
- 🍃 Spring Boot 3.2.2
- 🐘 PostgreSQL 15+
- 📦 Spring Data JPA + QueryDSL
- 🔐 Spring Security + JWT
- 🔄 Flyway (数据库迁移)

**前端**：
- 🖼️ Vue 3 + TypeScript
- ⚡ Vite
- 🎨 Element Plus
- 🗂️ Pinia (状态管理)
- 🛣️ Vue Router

**构建工具**：
- Maven
- npm/yarn

---

## 📦 项目结构

```
itop-java/
├── itop-common/          # 公共工具类和配置
├── itop-core/             # 核心域模型和数据访问层
│   ├── entity/            # JPA 实体类
│   ├── repository/        # Spring Data JPA 仓库
│   └── resources/db/      # Flyway 数据库迁移脚本
├── itop-cmdb/             # CMDB 业务逻辑模块
├── itop-api/              # REST API 层
│   ├── controller/        # REST 控制器
│   ├── dto/               # 数据传输对象
│   └── config/            # 配置类
└── itop-web/              # Vue 3 前端应用
    └── src/main/frontend/
        ├── src/
        │   ├── api/       # API 调用
        │   ├── views/     # 页面组件
        │   ├── router/    # 路由配置
        │   └── styles/    # 样式文件
        └── package.json
```

---

## 🚀 快速开始

### 前置要求

- ☕ JDK 21+
- 🐘 PostgreSQL 15+
- 📦 Maven 3.9+
- 🟢 Node.js 20+ & npm

### 方式一：Docker 部署（推荐）⭐

**最简单的部署方式！**

#### Windows:
```bash
deploy.bat build
deploy.bat start
```

#### Linux/macOS:
```bash
chmod +x quick-start.sh
./quick-start.sh
```

访问：`http://localhost`
- 用户名：`admin`
- 密码：`admin123`

📖 详细部署文档见 [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)

---

### 方式二：本地开发环境

### 1. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE itop;

-- 创建用户
CREATE USER itop WITH PASSWORD 'itop123';

-- 授权
GRANT ALL PRIVILEGES ON DATABASE itop TO itop;
```

### 2. 后端启动

```bash
cd F:/ITop-java

# 编译项目
mvn clean install

# 启动 API 服务
cd itop-api
mvn spring-boot:run
```

API 服务将在 `http://localhost:8080` 启动。

### 3. 前端启动

```bash
cd F:/ITop-java/itop-web/src/main/frontend

# 安装依赖
npm install

# 开发模式启动
npm run dev
```

前端应用将在 `http://localhost:5173` 启动。

---

## 📚 API 文档

启动后端后访问：
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`

### 主要端点

**认证**：
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出

**组织管理**：
- `GET /api/organizations` - 获取组织列表
- `GET /api/organizations/{id}` - 获取单个组织
- `POST /api/organizations` - 创建组织
- `PUT /api/organizations/{id}` - 更新组织
- `DELETE /api/organizations/{id}` - 删除组织

**服务器管理**：
- `GET /api/servers` - 获取服务器列表
- `GET /api/servers/{id}` - 获取单个服务器
- `POST /api/servers` - 创建服务器
- `PUT /api/servers/{id}` - 更新服务器
- `DELETE /api/servers/{id}` - 删除服务器

---

## 🔑 默认账号

管理员账号：
- 用户名: `admin`
- 密码: `admin123`

---

## 🏗️ 核心功能

### 已实现 ✅

- ✅ 用户认证与授权 (JWT)
- ✅ 组织管理 (CRUD)
- ✅ 服务器管理 (CRUD)
- ✅ 配置项基础模型
- ✅ 关系管理
- ✅ 数据库自动迁移 (Flyway)
- ✅ REST API (分页、过滤、排序)
- ✅ Vue 3 现代前端界面

### 待开发 🚧

- 🚧 完整的 CMDB 实体类型 (网络设备、应用等)
- 🚧 关系可视化
- 🚧 影响分析
- 🚧 审计日志
- 🚧 数据导入导出
- 🚧 高级搜索 (QueryDSL)
- 🚧 文件附件管理

---

## 🗄️ 数据模型

### 核心实体

**Organization** (组织)
- 支持层级结构
- 类型: company, department, team

**Contact** (联系人)
- 抽象基类
- 子类: Person, Team

**ConfigurationItem** (配置项)
- CMDB 核心
- 子类: Server, NetworkDevice, Application

**User** (用户)
- 认证与授权
- RBAC 权限模型

**LnKRelation** (关系)
- CI 之间的关系
- 类型: depends_on, connects_to, manages, etc.

---

## ⚙️ 配置

### 后端配置

编辑 `itop-api/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/itop
    username: itop
    password: itop123

jwt:
  secret: YourSecretKeyHere
  expiration: 86400000  # 24小时
```

### 前端配置

编辑 `itop-web/src/main/frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 📝 开发指南

### 添加新的实体类型

1. 在 `itop-core/entity/` 创建实体类
2. 在 `itop-core/repository/` 创建 Repository
3. 在 `itop-core/resources/db/migration/` 添加数据库迁移脚本
4. 在 `itop-api/controller/` 创建 REST 控制器
5. 在前端添加对应页面和 API 调用

### 数据库迁移

Flyway 脚本命名规范:
```
V{version}__{description}.sql
```

示例:
```
V1.0.0__Initial_Schema.sql
V1.0.1__Add_Team_Entity.sql
V1.0.2__Add_CI_Relations.sql
```

---

## 🔧 故障排查

### 数据库连接失败

```bash
# 检查 PostgreSQL 服务状态
pg_isready -h localhost -p 5432

# 测试连接
psql -h localhost -U itop -d itop
```

### 前端无法连接后端

检查 CORS 配置 (`SecurityConfig.java`) 和 API 基础 URL。

---

## 📄 许可证

AGPL-3.0 License

---

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

---

## 🙏 致谢

基于 iTop (https://github.com/Combodo/iTop) 核心概念重新设计和实现。

---

**Enjoy building your CMDB! 🎉**