# Frontend Structure

## Screen Inventory

- Authentication: Login
- Requester Portal: Home, New Request, All Requests, Ongoing Requests, Closed Requests, Request Detail
- IT Workspace: Workload Overview, Team Queue, My Tasks, Test Queue, Assignment, Request Detail
- Admin Console: Dashboard, Requests, Organizations, Users, Roles, Permissions, Teams, Routing Rules, Audit Logs
- Shared overlays: filter toolbar, assignment drawer/dialog, transition dialog, comment composer, create/edit Admin dialog, row action menu

## State Model

| State | Visible owner | Primary action | Next state |
| --- | --- | --- | --- |
| New | Team Lead / Admin | Assign | Assigned |
| Assigned | Technician | Start Work | In Progress |
| In Progress | Technician | Send to Testing | Testing |
| Testing | Tester | Pass Test / Return | Resolved / In Progress |
| Resolved | Requester | Close / User Test Failed | Closed / User Test Failed |
| User Test Failed | Technician | Resume Work | In Progress |
| Closed | Requester / Admin | View history | Terminal |

## Desktop Shell

```text
+----------------------+----------------------------------------------------+
| Product / role       | Context header                 account / environment|
|----------------------|----------------------------------------------------|
| Section navigation   | Page title                         primary action   |
|                      |----------------------------------------------------|
|                      | Metrics / filters / work controls                  |
|                      |----------------------------------------------------|
|                      | Main table, queue, form or detail       inspector   |
|                      | Independent vertical scroll                         |
+----------------------+----------------------------------------------------+
```

## Request Detail

```text
+--------------------------------------------------------------------------+
| Back | Request no. + title | status | adjacent workflow actions          |
|---------------------------------------------------------------------- ---|
| Workflow progression                                                    |
|------------------------------------------------+-------------------------|
| Description / public messages / work notes     | ownership and metadata  |
| Request history                                 | dates and priority      |
| Comment composer                               |                         |
+------------------------------------------------+-------------------------+
```

The sidebar and top context bar stay fixed. Page content scrolls. Tables use a bounded action column and never resize when controls change state.
