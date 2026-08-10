# Component Map

| UI element | Production source | Change | Related files |
| --- | --- | --- | --- |
| Admin / IT shell | MainLayout | Modify | `src/layouts/MainLayout.vue` |
| Requester shell | UserPortalLayout | Modify | `src/layouts/UserPortalLayout.vue` |
| Page heading | PageHeader | Modify | `src/components/PageHeader.vue` |
| Request status | RequestStatusTag | Reuse | `src/components/RequestStatusTag.vue` |
| Request flow | RequestStateFlow | Modify | `src/components/RequestStateFlow.vue` |
| Shared request table | New component | New | `src/components/RequestTable.vue` |
| Priority display | New component | New | `src/components/PriorityTag.vue` |
| Request fixture store | mockRequests | Modify | `src/data/mockRequests.ts` |
| Admin fixtures | New fixture module | New | `src/data/mockAdmin.ts` |
| Buttons / inputs / tables | Global Element Plus overrides | Modify | `src/styles/index.scss` |
| Icons | Element Plus icons | Reuse | Page and layout imports |

Preview-only fixture data must not be copied into backend-facing services. Components and routes can remain when the APIs replace fixture reads and writes.
