## 1. Architecture decisions (NEVER summarize)

* Decision: Use `/:pathMatch(.*)*` for 404 fallback routing in Vue Router.
* Reason: Vue Router 4 requires explicit regex for catch-all routes.
* Scope: Global routing layer.
* Constraint: Must be placed at the end of the routes array.
* Forbidden action: Do not use `*` or `/*` which are deprecated in Vue Router 4.
* Compatibility impact: Affects all undefined routes.
* Related files: `frontend/src/router/index.js`
* Rollback impact: Low.

* Decision: Exclude `responseType === 'blob'` from standard JSON `res.code !== 200` check in Axios.
* Reason: Blob responses for file downloads do not contain standard JSON structure.
* Scope: Global API request interceptor.
* Constraint: Must return `response` directly for blob requests.
* Forbidden action: Do not parse blob as JSON.
* Compatibility impact: Fixes file download APIs.
* Related files: `frontend/src/api/request.js`
* Rollback impact: Low.

* Decision: Wrap `JSON.parse` in `try-catch` inside `getUser` utility.
* Reason: Invalid JSON in `localStorage` causes application crash.
* Scope: Auth utility layer.
* Constraint: Must return `null` on parse failure.
* Forbidden action: Do not assume `localStorage` data is always valid JSON.
* Compatibility impact: Improves application stability.
* Related files: `frontend/src/utils/auth.js`
* Rollback impact: Low.

* Decision: Define global CSS variables in `main.css`.
* Reason: Ensures consistent UI theme (primary color, background, typography).
* Scope: Global styling.
* Constraint: Must be imported in `main.js`.
* Forbidden action: Do not hardcode these colors in individual components.
* Compatibility impact: Affects all Vue components relying on global variables.
* Related files: `frontend/src/assets/styles/main.css`, `frontend/src/main.js`
* Rollback impact: UI theme regression.

## 2. Modified files and their key changes (include path)

* Path: `frontend/src/router/index.js`
* Change type: MODIFY
* Key change: Added `/:pathMatch(.*)*` route.
* Core logic: Redirects unknown paths to NotFoundPage.
* Dependency impact: Relies on NotFoundPage.vue.
* Risk point: Must be the last route.
* Rollback method: Remove the route object.
* Pitfall: Placing before other routes causes false 404s.

* Path: `frontend/src/api/request.js`
* Change type: MODIFY
* Key change: Added blob response type check in interceptor.
* Core logic: Returns raw response if `responseType === 'blob'`.
* Dependency impact: None.
* Risk point: Missing `.data` handling in calling functions.
* Rollback method: Revert interceptor logic.
* Pitfall: Forgetting to set `responseType: 'blob'` in download API calls.

* Path: `frontend/src/utils/auth.js`
* Change type: MODIFY
* Key change: Added `try-catch` to `getUser`.
* Core logic: Catches JSON parse errors.
* Dependency impact: None.
* Risk point: Silently hides invalid data issues.
* Rollback method: Revert to direct `JSON.parse`.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/NotFoundPage.vue`
* Change type: ADD
* Key change: Created 404 page template.
* Core logic: Uses `el-result` to show 404 and provides navigation buttons.
* Dependency impact: Requires Element Plus and Vue Router.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/assets/styles/main.css`
* Change type: ADD
* Key change: Created global CSS file.
* Core logic: Defines CSS variables and common utility classes.
* Dependency impact: Imported by main.js.
* Risk point: Potential CSS conflict with Element Plus.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/main.js`
* Change type: MODIFY
* Key change: Imported `main.css`.
* Core logic: Injects global styles.
* Dependency impact: Relies on `main.css`.
* Risk point: Missing file causes build failure.
* Rollback method: Remove import statement.
* Pitfall: UNKNOWN.

## 3. Current verification status (PASS / FAIL / NOT RUN)

* Install: PASS
* Build: PASS
* Typecheck: NOT RUN
* Lint: PASS
* Unit tests: NOT RUN
* Integration tests: NOT RUN
* Contract tests: NOT RUN
* Manual verification: NOT RUN
* Current blocker: None
* Core error: None

## 4. Open TODOs and rollback notes

* TODO: Implement Visitor Pages (Phase 7).
* Priority: HIGH
* Required prerequisite: Phase 6 completed.
* Target files: `HomePage.vue`, `CourseListPage.vue`, `CourseDetailPage.vue`, `EnrollmentPage.vue`.
* Risk: Low.
* Suggested action: Implement UI components following Phase 3 plan.
* Rollback files: All Phase 6 modified files.
* Rollback command: `git restore frontend/src/`
* Post-rollback verification: `pnpm build`

## 5. Tool outputs (keep PASS / FAIL only)

* Tool: pnpm lint
* Command: `cd frontend && pnpm lint`
* Result: PASS
* Failure reason: None
* Related files: All frontend files
* Blocking next phase: NO

* Tool: pnpm build
* Command: `cd frontend && pnpm build`
* Result: PASS
* Failure reason: None
* Related files: All frontend files
* Blocking next phase: NO

## 6. List next phase detailed planning

* Next phase goal: Implement Phase 7 Visitor Pages.
* Entry point: `frontend/src/views/public/HomePage.vue`
* Input prerequisite: Phase 6 completed.
* Step: Implement `BannerCarousel.vue` and `CourseCard.vue`.
* Target files: `frontend/src/components/home/BannerCarousel.vue`, `frontend/src/components/home/CourseCard.vue`.
* Expected change: Reusable components created.
* Verification method: `pnpm lint` and `pnpm build`.
* Acceptance criteria: Build succeeds without lint errors.
* Risk control: Keep components lightweight.
* Forbidden action: Do not add business logic to UI components.

* Next phase goal: Assemble Visitor Pages.
* Entry point: `HomePage.vue`, `CourseListPage.vue`, `CourseDetailPage.vue`, `EnrollmentPage.vue`.
* Input prerequisite: Reusable components implemented.
* Step: Assemble pages and bind mock/real API calls.
* Target files: `frontend/src/views/public/*`
* Expected change: Pages render correctly with data.
* Verification method: Manual UI test, `pnpm build`.
* Acceptance criteria: Pages match layout design and build succeeds.
* Risk control: Use Element Plus components.
* Forbidden action: Do not bypass Vue Router for navigation.
