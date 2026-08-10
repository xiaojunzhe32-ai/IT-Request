# 技术负责人 / Team Lead 需求跟踪

技术负责人管理团队队列、分配和重新分配请求、调整状态并观察工作量。Admin 可执行相同的全局管理动作，但 Tester 权限与 Team Lead 独立。

## 主观进度

| 区域 | 状态 | 完成度 | 当前证据 | 主要缺口 |
| --- | --- | --- | --- | --- |
| 团队与队列 | 进行中 | 94% | 真实 Team DTO/User DTO 和 Team Queue | 多团队 scope 待确认；Docker 回归 |
| Assignment Desk | 进行中 | 97% | 未分配置顶，其次按优先级和更新时间排序；真实路由规则 | Docker 回归 |
| 状态与分配 | 进行中 | 97% | 跨团队人员、任意状态、单一 Save、历史记录 | Docker 回归 |
| Workload Overview | 进行中 | 95% | 从真实请求 API 计算 open/testing/user failed/unassigned | 专用统计接口可后续优化 |

## 功能需求跟踪

| ID | 需求 | 优先级 | 状态 | 完成度 | 实现证据 | 遇到的问题 | 待说明/待确认 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| TL-001 | 查看团队队列 | P0 | 进行中 | 94% | `TeamQueue.vue` + request/team API | Docker daemon 未运行 | 多团队负责人 scope |
| TL-002 | 分配新请求并优先查看未分配项 | P0 | 进行中 | 97% | `Assignment.vue` 未分配置顶，再按优先级排序 | Docker 回归 | 已确认未分配优先 |
| TL-003 | 重新分配/转交请求 | P0 | 进行中 | 97% | Request Detail 真实 assign API，选项显示 team/org | Docker 回归 | 已确认允许跨团队 |
| TL-004 | 选择任意请求状态 | P0 | 进行中 | 97% | 全状态下拉和统一 Save | Docker 回归 | 已确认不限制跳步 |
| TL-005 | 代技术人员提交测试 | P0 | 进行中 | 97% | 可选择 `Testing`，备注可选 | Docker 回归 | 已确认不强制备注 |
| TL-006 | Tester 权限独立分配 | P1 | 进行中 | 95% | Team Lead 和 Tester 为独立角色/权限 | Docker 权限回归 | 已确认拆分权限 |
| TL-007 | 查看团队工作量 | P1 | 进行中 | 95% | Overview 使用真实请求数据 | 可增加专用统计 API | 指标可按运营反馈扩展 |
| TL-008 | 所有负责人动作写入历史 | P0 | 进行中 | 96% | 分配、状态、评论、附件均记录 history | Docker 数据回归 | 无 |

## 已确认与待确认

已确认：Assignment Desk 未分配请求优先；允许跨团队转派；状态可任意选择；变更说明不必填；Team Lead 和 Tester 权限拆分。

待确认：Team Lead 是否可以负责多个团队，以及对应 Team Queue 的 scope。Docker Desktop 恢复后需验证路由、分配、排序和工作量数据。
