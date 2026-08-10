# Admin 需求跟踪

Admin 是权限最大的系统管理者，也可以作为全局技术负责人。Admin 负责组织、账号、权限、团队、路由、分配和审计，但不是请求流程中的必经节点。

## 主观进度

| 区域 | 状态 | 完成度 | 当前证据 | 主要缺口 |
| --- | --- | --- | --- | --- |
| 登录与导航 | 进行中 | 97% | 真实 JWT、`/auth/me`、按角色导航；产品页面全英文 | Docker 登录回归 |
| 组织/用户/角色/权限 | 进行中 | 96% | 管理页面和 `src/api/system.ts` 均使用真实 CRUD API | Docker 保存/刷新回归 |
| 团队管理 | 进行中 | 95% | `team_user_member`、`leader_user_id`、真实成员和负责人表单 | V1.0.25 实库验证 |
| Routing Rules | 进行中 | 96% | 真实列表、创建、编辑、启用/禁用、排序和自动匹配 | Docker 自动路由回归 |
| Requests / Audit / Dashboard | 进行中 | 96% | 全局请求、审计日志、真实统计均已接 API | Docker 数据回归 |

## 功能需求跟踪

| ID | 需求 | 优先级 | 状态 | 完成度 | 实现证据 | 遇到的问题 | 待说明/待确认 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ADM-001 | Admin 登录并进入 Admin Console / IT Workspace | P0 | 进行中 | 97% | `admin/admin123`、JWT、`/auth/me`、真实路由 | Docker daemon 未运行 | 无 |
| ADM-002 | 管理组织 | P0 | 进行中 | 96% | `OrganizationList.vue` + organization API CRUD | 容器端待验证 | 组织层级深度可后续配置 |
| ADM-003 | 管理用户和启用/禁用 | P0 | 进行中 | 96% | `UserList.vue` + user API；表单可分配组织和角色 | 容器端待验证 | 是否增加物理删除 |
| ADM-004 | 管理角色和权限 | P0 | 进行中 | 96% | `RoleList.vue`、`PermissionList.vue` + 真实 API | 容器端待验证 | 无 |
| ADM-005 | 管理团队、负责人和成员 | P0 | 进行中 | 95% | `TeamList.vue`、V1.0.25、Team DTO/User DTO | Flyway 未实库执行 | Team Lead 多团队范围 |
| ADM-006 | 配置请求路由规则 | P0 | 进行中 | 96% | `RoutingRules.vue` + RoutingRule CRUD/匹配服务 | 容器端待验证 | 无 |
| ADM-007 | 查看、分配并调整所有请求 | P0 | 进行中 | 97% | Global Requests、Request Detail、单一 Save、真实 API | 容器端待验证 | 状态原因已确认可选 |
| ADM-008 | 查看审计日志 | P0 | 进行中 | 95% | `AuditLogList.vue` + JPA Criteria 查询 | 容器端待验证 | 导出是否列为 P1 |
| ADM-009 | Admin 页面全部英文 | P0 | 已完成 | 100% | 新 Admin/Workspace 可见页面文本 | 旧隐藏文件不纳入范围 | 无 |
| ADM-010 | 不显示 FAQ、服务台、Problem、Known Error、Change | P0 | 已完成 | 100% | 新菜单和路由不暴露；旧文件保留隐藏 | 无 | 已确认保留隐藏 |

## 页面范围

| 页面 | 当前状态 | 数据来源 |
| --- | --- | --- |
| Admin Overview | 进行中 | Dashboard API |
| Organizations | 进行中 | Organization CRUD API |
| Users | 进行中 | User CRUD API |
| Roles / Permissions | 进行中 | Role / Permission API |
| Teams | 进行中 | Team CRUD API |
| Routing Rules | 进行中 | Routing Rule API |
| Requests | 进行中 | Request API |
| Audit Logs | 进行中 | Audit API |

## 已确认与待确认

已确认：状态变更说明可选；Team Lead 和 Tester 权限拆分；旧功能文件保留隐藏；用户管理当前采用禁用优先。

待确认：是否需要用户物理删除、审计日志导出、Team Lead 多团队 scope。Docker Desktop 恢复后需完成所有写操作和 Flyway 回归。
