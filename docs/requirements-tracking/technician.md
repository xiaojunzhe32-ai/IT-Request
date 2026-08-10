# 技术处理人员 / Technician 需求跟踪

技术处理人员处理请求、添加内部或公开留言、在 IT 人员之间转派，并将修复完成的请求交给测试组。

## 主观进度

| 区域 | 状态 | 完成度 | 当前证据 | 主要缺口 |
| --- | --- | --- | --- | --- |
| My Tasks | 进行中 | 96% | 真实 assignee 查询；列内按 Critical/High/Medium/Low 和更新时间排序 | Docker 回归 |
| Team Queue | 进行中 | 92% | 真实请求和团队 API | 团队可见 scope 待确认 |
| 状态与转派 | 进行中 | 97% | 任意状态下拉、跨团队人员选项、单一 Save、未保存离开提示 | Docker 回归 |
| 留言、附件与历史 | 进行中 | 96% | public/internal comment、附件和 history 均走真实 API | Docker 权限回归 |

## 功能需求跟踪

| ID | 需求 | 优先级 | 状态 | 完成度 | 实现证据 | 遇到的问题 | 待说明/待确认 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| TECH-001 | 在 My Tasks 查看自己的请求并优先处理高优先级 | P0 | 进行中 | 96% | `MyTasks.vue` 按优先级、更新时间排序 | Docker daemon 未运行 | 已确认优先级优先 |
| TECH-002 | 查看团队可见请求 | P0 | 进行中 | 90% | `TeamQueue.vue` + request/team API | scope 未最终确定 | 全 IT 或团队内可见 |
| TECH-003 | 通过下拉选择任意状态 | P0 | 进行中 | 97% | Request Detail 全状态下拉和统一 Save | Docker 回归 | 已确认不限制跳步、不强制说明 |
| TECH-004 | 转派给其他 IT 处理人员 | P0 | 进行中 | 97% | Assignee 选项显示 team/org，真实 assign API | Docker 回归 | 已确认允许跨团队 |
| TECH-005 | 修复完成后转入 `Testing` | P0 | 进行中 | 97% | 选择 `Testing` 后写状态和 history | Docker 回归 | 不强制 fix summary |
| TECH-006 | 添加内部工作备注及附件 | P0 | 进行中 | 96% | internal comment + comment attachment API | Docker 权限回归 | 非 IT 不可见 |
| TECH-007 | 给用户添加公开留言及附件 | P1 | 进行中 | 96% | public comment + attachment API | 通知未做 | 当前只做留言机制 |
| TECH-008 | `User Test Failed` 后重新转为 `In Progress` | P0 | 进行中 | 97% | 状态下拉与真实 transition API | Docker 回归 | 无 |
| TECH-009 | 每次变更同步请求历史 | P0 | 进行中 | 96% | 创建、分配、状态、评论、附件均记录 history | Docker 数据回归 | 无 |

## 已确认与待确认

已确认：My Tasks 优先级优先；IT 状态可任意选择且不强制备注；允许跨团队转派；只做留言、不做实时聊天；留言支持附件。

待确认：Team Queue 的查看范围是全 IT 还是团队内。Docker Desktop 恢复后需验证跨团队分配、内部备注隔离和附件访问。
