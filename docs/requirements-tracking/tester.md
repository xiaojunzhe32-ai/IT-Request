# 测试人员 / Tester 需求跟踪

测试人员负责 IT 内部复测。通过后把请求改为 `Resolved`；失败后改回 `In Progress`。测试人员与技术负责人是独立权限。

## 主观进度

| 区域 | 状态 | 完成度 | 当前证据 | 主要缺口 |
| --- | --- | --- | --- | --- |
| Test Queue | 进行中 | 95% | 真实查询 `Testing` 请求 | Docker 回归；提交测试时间排序可后续优化 |
| 测试结果 | 进行中 | 96% | 全状态下拉、统一 Save；通过/失败均写一条状态历史 | Docker 回归 |
| 备注和可见性 | 进行中 | 95% | 可查看全部内部备注；可选留言和附件 | Docker 权限回归 |

## 功能需求跟踪

| ID | 需求 | 优先级 | 状态 | 完成度 | 实现证据 | 遇到的问题 | 待说明/待确认 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| TEST-001 | 查看 `Testing` 请求 | P0 | 进行中 | 95% | `TestQueue.vue` 调用真实 Request API | Docker daemon 未运行 | 后续可按 submittedToTestingAt 排序 |
| TEST-002 | 打开详情并查看全部内部备注 | P0 | 进行中 | 96% | 共用 Request Detail；IT 权限可见 internal comments | Docker 回归 | 已确认可以查看 |
| TEST-003 | 测试通过后改为 `Resolved` | P0 | 进行中 | 96% | 状态下拉 + transition API | Docker 回归 | 测试 note 已确认不必填 |
| TEST-004 | 测试失败后改回 `In Progress` | P0 | 进行中 | 96% | 状态下拉 + transition API；可选留言/附件 | Docker 回归 | 原因输入可保留但不必填 |
| TEST-005 | 不新增单独 `Test Failed` 状态 | P0 | 已完成 | 100% | 前后端状态枚举均无 `Test Failed` | 无 | 已确认 |
| TEST-006 | 测试结果只写一条历史事件 | P0 | 进行中 | 96% | transition 写一条 `STATUS_CHANGED` | Docker 数据回归 | 已确认一条 |
| TEST-007 | Tester 状态选择不受相邻步骤限制 | P0 | 进行中 | 97% | 统一全状态下拉 | Docker 回归 | 已确认不限制 |

## 已确认与待确认

已确认：Tester 可看全部内部备注；测试说明不必填；失败原因可选；不新增 `Test Failed`；测试动作只写一条历史；状态下拉不限制；Team Lead 与 Tester 权限拆分。

当前没有产品待确认项。Docker Desktop 恢复后完成测试通过、失败、附件和内部备注端到端验证。
