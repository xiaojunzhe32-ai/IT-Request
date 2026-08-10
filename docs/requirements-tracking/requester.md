# 普通用户 / Requester 需求跟踪

普通用户提交请求、查看有权限访问的请求、公开留言，并在技术处理和内部测试完成后确认关闭或标记 `User Test Failed`。

## 主观进度

| 区域 | 状态 | 完成度 | 当前证据 | 主要缺口 |
| --- | --- | --- | --- | --- |
| Portal 与登录 | 进行中 | 97% | 真实账号、JWT、Portal 首页和列表 | Docker 角色回归 |
| 新建请求 | 进行中 | 97% | 真实创建 API、富文本、粘贴图片、附件、时间与影响系统字段 | Docker 上传回归 |
| 详情、留言与附件 | 进行中 | 96% | 真实详情/评论/历史 API；留言附件可上传下载 | Docker 文件回归 |
| 用户确认 | 进行中 | 97% | Portal 仅在 `Resolved` 提供 `Closed` / `User Test Failed`；后端再次校验 | Docker 状态回归 |

## 功能需求跟踪

| ID | 需求 | 优先级 | 状态 | 完成度 | 实现证据 | 遇到的问题 | 待说明/待确认 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| REQ-001 | 登录并进入 Portal | P0 | 进行中 | 97% | `requester01/admin123`、JWT、`/auth/me` | Docker daemon 未运行 | 无 |
| REQ-002 | 创建轻量但信息完整的新请求 | P0 | 进行中 | 97% | Title、Type、Affected Service、Priority、Organization、Occurrence Time、Requested Resolution Time、富文本和附件均走真实 API | Docker 上传待验证 | 后续继续参考同类产品优化字段体验 |
| REQ-003 | 新请求从 `New` 开始或经规则成为 `Assigned` | P0 | 进行中 | 96% | RequestService 生成编号并执行真实 routing rule | Docker 自动路由待验证 | 无 |
| REQ-004 | 查看自己的请求 | P0 | 进行中 | 97% | Portal 列表按 caller 查询真实分页数据 | Docker 回归 | 无 |
| REQ-005 | 只读查看其他用户的请求 | P0 | 进行中 | 88% | 后端按组织范围做访问控制 | 详情字段粒度未定 | 他人描述/公开留言可见范围 |
| REQ-006 | 添加公开留言并附带附件 | P0 | 进行中 | 97% | Comment API + Attachment API；服务端按请求 scope 校验 | Docker 上传下载待验证 | 已确认支持附件 |
| REQ-007 | 不显示内部备注 | P0 | 进行中 | 96% | 非 IT 用户的 comment/history 查询过滤 internal | Docker 权限回归 | 无 |
| REQ-008 | 从 `Resolved` 关闭请求 | P0 | 进行中 | 97% | Portal 状态下拉和后端都限制来源状态 | Docker 回归 | Close note 已确认可选 |
| REQ-009 | 从 `Resolved` 标记 `User Test Failed` | P0 | 进行中 | 97% | Portal 状态下拉和后端校验；可通过留言添加附件 | Docker 回归 | 失败原因已确认可选 |
| REQ-010 | Portal 不显示 FAQ / Knowledge Base | P0 | 已完成 | 100% | 新路由和导航不包含 FAQ | 无 | 旧文件已确认保留隐藏 |

## 页面范围

| 页面 | 当前状态 | 说明 |
| --- | --- | --- |
| Home | 进行中 | 真实请求指标和活跃请求 |
| New Request | 进行中 | 真实富文本、粘贴图片和附件上传 |
| All / Ongoing / Closed Requests | 进行中 | 真实分页查询 |
| Request Detail | 进行中 | 详情、公开留言、附件、历史、用户确认 |
| FAQ | 本期不做 | 保留旧文件但不暴露 |

## 已确认与待确认

已确认：留言支持附件；Close note 可选；`User Test Failed` 原因可选且允许附件；FAQ 保留隐藏。

待确认：普通用户查看他人请求时，完整描述和公开留言的可见粒度。Docker Desktop 恢复后需验证上传、下载和两种用户确认状态。
