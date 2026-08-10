# Screen Contract

| Screen | Purpose | Key regions | States | PRD source |
| --- | --- | --- | --- | --- |
| Login | Enter the correct role workspace | Brand rail, credentials, demo profile | Empty, validation, loading, failure | 6, 12.4 |
| Portal Home | Show requester priorities | Confirmation queue, counters, recent requests | Empty, ongoing, resolved pending | 6.1 |
| New Request | Submit a lightweight routed request | Basics form, rich description editor, pasted images, attachment area, routing preview | Draft, validation, submitted | 6.1, 7.1-7.6 |
| Portal Lists | Inspect public requests | Scope tabs, filters, request table | All, ongoing, closed, no results | 5.1, 6.1 |
| Request Detail | Operate one request without status jumps | Flow, detail, messages, history, role actions | All seven statuses | 3, 4, 7.4-7.6 |
| Workload Overview | Monitor team work | Status counters, assignee workload, queue summary | Normal, overloaded, empty | 6.2 |
| Team / My / Test Queues | Repeated request handling | Scope header, filters, request table | Active scopes, no results | 6.2, 9.3 |
| Assignment | Assign and transfer requests | Request selector, team, assignee, tester | Unassigned, assigned, reassign | 5.2, 6.2 |
| Admin Dashboard | Monitor platform and workflow | Workflow counters, team load, recent events | Normal, warning | 2.2, 6.3 |
| Admin setup pages | Manage access and routing | Filter bar, data table, edit overlay | List, filter, create, edit | 2.2, 6.3 |
| Audit Logs | Trace sensitive changes | Filters, append-only event table | Results, no results | 8.2 |

## Assumptions

- Team Lead and Tester permissions may overlap, but their actions remain status-driven.
- Request assignment is a dedicated workspace page and also available from Request Detail.
- Fixture actions update only the current browser session.
