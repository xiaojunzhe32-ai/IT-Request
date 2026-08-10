# Admin Module Implementation Plan

## Goal
Realign the existing project to the IT Request Workflow requirements. Start with Admin Console: Organization Management, User Management, Role/Permission Management, Team Management. All UI in English.

## Phase 1: Database Migration (V1.0.17)

**File**: `itop-core/src/main/resources/db/migration/V1.0.17__Workflow_Roles_And_Permissions.sql`

- Delete old demo users (id 10-18) and their role assignments
- Update existing 3 roles to new 5-role system:
  - `ADMIN` -> keep, permissions: `["*"]`
  - `CMDB_ADMIN` -> repurpose as `TEAM_LEAD`, permissions: `["request:*","team:read","team:write","user:read"]`
  - `USER` -> repurpose as `REQUESTER`, permissions: `["request:create","request:read"]`
  - Insert new: `TECHNICIAN` permissions: `["request:*","team:read"]`
  - Insert new: `TESTER` permissions: `["request:*","team:read"]`
- Update `admin` user (id=1) to have ADMIN role
- Insert demo users for each new role with password `admin123`
- Assign roles to demo users

## Phase 2: Backend Updates

### 2a. SecurityUtils - add convenience role checks
- `isRequester()`, `isTechnician()`, `isTester()`, `isTeamLead()` methods

### 2b. RoleController - update permissions dictionary
Replace old permission list with new workflow permissions:
```
request:create, request:read, request:write, request:delete, request:assign,
request:transfer, request:transition, request:test,
user:read, user:write, user:delete,
role:read, role:write,
org:read, org:write,
team:read, team:write,
audit:read, admin:* (wildcard)
```

### 2c. All controllers - update @PreAuthorize annotations
- Old: `@PreAuthorize("@securityUtils.hasPermission('ticket:read')")`
- New: `@PreAuthorize("@securityUtils.hasPermission('request:read')")` or admin-only
- Organization: `org:read` / `org:write`
- User: `user:read` / `user:write`
- Role: `role:read` / `role:write`
- Team: `team:read` / `team:write`

## Phase 3: Frontend - Admin Console

### 3a. Router rewrite (`router/index.ts`)
- Remove all old routes (cmdb, service, tickets, portal faq, etc.)
- Keep: `/login`, `/portal` (placeholder for later), `/` (admin layout)
- Admin routes (all English titles):
  - `/dashboard` -> Dashboard
  - `/organizations` -> Organizations
  - `/users` -> Users
  - `/roles` -> Roles
  - `/teams` -> Teams
  - `/audit-logs` -> Audit Logs

### 3b. MainLayout rewrite (`MainLayout.vue`)
- Branding: "IT Request System"
- Menu groups (all English):
  - Dashboard
  - Administration (Organizations, Users, Roles, Teams)
  - Audit Logs
- Remove CMDB, Service, Ticket menu groups

### 3c. Login page (`login/index.vue`)
- English: "Username", "Password", "Login"
- Default hint: "admin / admin123"

### 3d. OrganizationList.vue - English labels
### 3e. UserList.vue - English labels
### 3f. RoleList.vue - English labels + new permissions
### 3g. TeamList.vue - English labels
### 3h. Dashboard - English, simplified for admin overview

## Out of Scope (for now)
- Request entity and workflow (next phase)
- Portal layout (next phase)
- IT Workspace (next phase)
- Routing Rules (next phase)
- Audit Logs page UI (backend exists, frontend later)
