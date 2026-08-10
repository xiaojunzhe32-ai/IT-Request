# IT Request Frontend UI Handoff

- Source PRD: `request-workflow-backend-requirements.md`
- UI reference: current iTop-inspired shell and user screenshots from 2026-08-08
- Output mode: `project-native-preview`
- Structure status: `structure-confirmed`
- Frontend project: `itop-web/src/main/frontend`
- Target viewports: 1440x900 and 1280x800 desktop, with a usable compact layout below 1024px

## Discovered UI Constraints

- Vue 3, Vue Router, Element Plus and Element Plus icons are already installed.
- The product uses a dark navigation rail, a light workspace and orange as the primary action color.
- The existing page header duplicates primary actions on several Admin pages; each action must have one owner.
- Existing buttons and table actions are visually cramped. Global controls use an 8px radius and row actions move into compact menus.
- The Windows-first font stack uses Segoe UI Variable Text / Segoe UI before generic fallbacks.
- FAQ, Service Desk, Problem, Known Error, Change and SLA pages remain outside the active route tree.
- Frontend behavior is backed by fixture data for this phase; API integration is a later boundary.
- All visible application copy is English.

## Run

```powershell
cd F:\ITop-java\itop-web\src\main\frontend
npm run dev
```

The Vite entry is `http://localhost:3000/login`. The Docker entry remains `http://localhost/login` after rebuilding `itop-web`.

## Migration Boundary

The route structure, components, layout and interaction states are production-aligned. Fixture mutations are in-memory preview behavior and must be replaced by request, transition, assignment, comment and Admin APIs.
