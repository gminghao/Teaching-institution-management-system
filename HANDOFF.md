# HANDOFF.md

> Project handoff report — generated 2026-06-11
> Current branch: `dev` | Base branch: `main`
> Progress: 58/78 tasks (74.4%)

---

## 1. Architecture decisions (NEVER summarize)

* Decision: B/S architecture with frontend-backend separation, RESTful API interaction.
* Reason: Clear separation of concerns, independent deployment.
* Scope: Full stack.
* Constraint: Frontend cannot contain domain logic or persistence.
* Forbidden action: Direct database access from frontend.
* Compatibility impact: None.
* Related files: All.
* Rollback impact: N/A.

* Decision: Three-layer architecture: Controller → Service → Mapper.
* Reason: Standard enterprise pattern, testable and maintainable.
* Scope: Backend.
* Constraint: Controller must not contain SQL or ORM calls.
* Forbidden action: Business logic in Controller.
* Compatibility impact: None.
* Related files: `backend/src/main/java/com/institution/coursemanager/`.
* Rollback impact: N/A.

* Decision: JWT authentication for admin endpoints.
* Reason: Stateless authentication, no server-side session.
* Scope: Admin API.
* Constraint: Token stored in localStorage, validated by interceptor.
* Forbidden action: Access admin API without valid token.
* Compatibility impact: All `/api/admin/**` endpoints require token.
* Related files: `interceptor/AdminAuthInterceptor.java`, `util/JwtUtil.java`.
* Rollback impact: Remove interceptor registration in `WebMvcConfig.java`.

* Decision: MyBatis-Plus ORM with logical delete.
* Reason: Convention over configuration, soft delete for data integrity.
* Scope: All entities.
* Constraint: `deleted` field for logical delete, `@TableLogic` annotation.
* Forbidden action: Physical delete on entities with `@TableLogic`.
* Compatibility impact: All queries auto-filter `deleted=1`.
* Related files: All entity classes.
* Rollback impact: N/A.

* Decision: Unified response format `Result<T>`.
* Reason: Consistent API contract for frontend.
* Scope: All API endpoints.
* Constraint: All responses wrapped in `Result<T>` with `code`, `message`, `data`.
* Forbidden action: Return raw data without wrapper.
* Compatibility impact: Frontend relies on this format.
* Related files: `vo/Result.java`, `vo/PageResult.java`.
* Rollback impact: N/A.

* Decision: Visitor controller package named `visitor` instead of `public`.
* Reason: `public` is Java reserved keyword, cannot be used as package name.
* Scope: Controller layer.
* Constraint: Must use `controller/visitor/` for public-facing endpoints.
* Forbidden action: Create package named `public`.
* Compatibility impact: None.
* Related files: `controller/visitor/PublicCourseController.java`, `controller/visitor/PublicEnrollmentController.java`.
* Rollback impact: N/A.

* Decision: CSS variables for global theme.
* Reason: Consistent UI styling across components.
* Scope: Frontend.
* Constraint: Must import `main.css` in `main.js`.
* Forbidden action: Hardcode theme colors in components.
* Compatibility impact: Affects all Vue components.
* Related files: `frontend/src/assets/styles/main.css`, `frontend/src/main.js`.
* Rollback impact: UI theme regression.

* Decision: Vue Router catch-all route `/:pathMatch(.*)*` for 404.
* Reason: Vue Router 4 requires explicit regex for catch-all.
* Scope: Frontend routing.
* Constraint: Must be last route in array.
* Forbidden action: Use deprecated `*` or `/*`.
* Compatibility impact: All undefined routes.
* Related files: `frontend/src/router/index.js`.
* Rollback impact: Low.

* Decision: Axios blob response exemption.
* Reason: Blob downloads don't have standard JSON structure.
* Scope: API request interceptor.
* Constraint: Return raw response for `responseType === 'blob'`.
* Forbidden action: Parse blob as JSON.
* Compatibility impact: Fixes file download APIs.
* Related files: `frontend/src/api/request.js`.
* Rollback impact: Low.

---

## 2. Modified files and their key changes (include path)

* Path: `backend/src/main/java/com/institution/coursemanager/entity/AdminUser.java`
* Change type: ADD
* Key change: Admin user entity with BCrypt password field.
* Core logic: Maps to `admin_user` table.
* Dependency impact: Used by AuthService.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/entity/Course.java`
* Change type: ADD
* Key change: Course entity with `@TableLogic` on `deleted` field.
* Core logic: Logical delete support.
* Dependency impact: Used by CourseService.
* Risk point: Queries auto-filter deleted records.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/entity/EnrollmentOrder.java`
* Change type: ADD
* Key change: Enrollment order entity with status fields.
* Core logic: Stores enrollment and payment status.
* Dependency impact: Used by EnrollmentService, PaymentService.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/service/impl/CourseServiceImpl.java`
* Change type: ADD
* Key change: Course CRUD + online/offline + pagination with page size limit (100).
* Core logic: Status validation, logical delete check.
* Dependency impact: Uses CourseMapper, CourseCategoryMapper.
* Risk point: N+1 query on category name.
* Rollback method: Delete file.
* Pitfall: getCategoryName() queries per record.

* Path: `backend/src/main/java/com/institution/coursemanager/service/impl/EnrollmentServiceImpl.java`
* Change type: ADD
* Key change: Enrollment submission + status transition validation + page size limit (100).
* Core logic: Order number generation, status state machine.
* Dependency impact: Uses CourseMapper, EnrollmentOrderMapper.
* Risk point: Order number collision in high concurrency.
* Rollback method: Delete file.
* Pitfall: Random suffix only 4 digits.

* Path: `backend/src/main/java/com/institution/coursemanager/service/impl/PaymentServiceImpl.java`
* Change type: ADD
* Key change: Payment recording with `@Transactional`.
* Core logic: Update order paid_amount and payment_status.
* Dependency impact: Uses EnrollmentOrderMapper, PaymentRecordMapper.
* Risk point: Concurrent payment may cause lost update.
* Rollback method: Delete file.
* Pitfall: No optimistic/pessimistic lock.

* Path: `backend/src/main/java/com/institution/coursemanager/service/impl/FinanceServiceImpl.java`
* Change type: ADD
* Key change: Finance summary using SQL aggregation query.
* Core logic: Single SQL query instead of full table load.
* Dependency impact: Uses EnrollmentOrderMapper.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/mapper/EnrollmentOrderMapper.java`
* Change type: ADD
* Key change: Added `selectFinanceSummary()` aggregation query.
* Core logic: SQL SUM/COUNT with CASE for status breakdown.
* Dependency impact: Used by FinanceServiceImpl.
* Risk point: None.
* Rollback method: Revert to original.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/resources/mapper/CourseMapper.xml`
* Change type: ADD
* Key change: JOIN query for course + category.
* Core logic: `selectPublicPage` and `selectAdminPage` with dynamic conditions.
* Dependency impact: Used by CourseMapper interface.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/controller/admin/AdminEnrollmentController.java`
* Change type: ADD
* Key change: Enrollment list and status update endpoints.
* Core logic: Parameter order fixed (paymentStatus, enrollmentStatus).
* Dependency impact: Uses EnrollmentService.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/controller/visitor/PublicCourseController.java`
* Change type: ADD
* Key change: Public course list and detail endpoints.
* Core logic: No auth required.
* Dependency impact: Uses CourseService.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/exception/GlobalExceptionHandler.java`
* Change type: ADD
* Key change: Unified exception handling with `@RestControllerAdvice`.
* Core logic: Maps BusinessException to Result error codes.
* Dependency impact: All controllers.
* Risk point: Returns HTTP 200 for all business errors.
* Rollback method: Delete file.
* Pitfall: Frontend must check `res.code` not HTTP status.

* Path: `backend/src/main/java/com/institution/coursemanager/interceptor/AdminAuthInterceptor.java`
* Change type: ADD
* Key change: JWT token validation for admin endpoints.
* Core logic: Extract Bearer token, validate via JwtUtil.
* Dependency impact: Registered in WebMvcConfig.
* Risk point: Swallowed exception (no logging).
* Rollback method: Remove from WebMvcConfig.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/config/WebMvcConfig.java`
* Change type: MODIFY
* Key change: Added AdminAuthInterceptor registration.
* Core logic: Intercepts `/api/admin/**` except login.
* Dependency impact: Depends on AdminAuthInterceptor.
* Risk point: None.
* Rollback method: Revert to original.
* Pitfall: CORS allows all origins.

* Path: `frontend/src/router/index.js`
* Change type: MODIFY
* Key change: Added 404 catch-all route.
* Core logic: `/:pathMatch(.*)*` redirects to NotFoundPage.
* Dependency impact: Requires NotFoundPage.vue.
* Risk point: Must be last route.
* Rollback method: Remove route object.
* Pitfall: UNKNOWN.

* Path: `frontend/src/api/request.js`
* Change type: MODIFY
* Key change: Added blob response exemption.
* Core logic: Return raw response for blob downloads.
* Dependency impact: None.
* Risk point: None.
* Rollback method: Revert interceptor logic.
* Pitfall: UNKNOWN.

* Path: `frontend/src/utils/auth.js`
* Change type: MODIFY
* Key change: Added try-catch in `getUser()`.
* Core logic: Catch JSON parse errors, return null.
* Dependency impact: None.
* Risk point: Silently hides invalid data.
* Rollback method: Revert to direct JSON.parse.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/NotFoundPage.vue`
* Change type: ADD
* Key change: 404 page with Element Plus `el-result`.
* Core logic: Navigation buttons to home/back.
* Dependency impact: Requires Element Plus.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/assets/styles/main.css`
* Change type: ADD
* Key change: Global CSS variables and utility classes.
* Core logic: Theme colors, typography, spacing.
* Dependency impact: Imported by main.js.
* Risk point: Potential conflict with Element Plus.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/public/HomePage.vue`
* Change type: ADD
* Key change: Home page with banner, intro, course cards.
* Core logic: Uses mock data (not API).
* Dependency impact: Uses PublicLayout.
* Risk point: Mock data not real.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/public/CourseListPage.vue`
* Change type: ADD
* Key change: Course list with search, filter, pagination.
* Core logic: Uses mock data (not API).
* Dependency impact: Uses PublicLayout.
* Risk point: Mock data not real.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/public/CourseDetailPage.vue`
* Change type: ADD
* Key change: Course detail page.
* Core logic: Uses mock data (not API).
* Dependency impact: Uses PublicLayout.
* Risk point: Mock data not real.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/public/EnrollmentPage.vue`
* Change type: ADD
* Key change: Enrollment form with validation.
* Core logic: Submit sets `submitted=true`, no API call.
* Dependency impact: Uses PublicLayout.
* Risk point: No real submission.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/admin/LoginPage.vue`
* Change type: ADD
* Key change: Admin login page.
* Core logic: Sets mock token, no real API call.
* Dependency impact: Uses auth utils.
* Risk point: No real authentication.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/admin/DashboardPage.vue`
* Change type: ADD
* Key change: Dashboard with stats cards.
* Core logic: Uses mock data (not API).
* Dependency impact: Uses AdminLayout.
* Risk point: Mock data not real.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/admin/CourseManagePage.vue`
* Change type: ADD
* Key change: Course management with CRUD dialog.
* Core logic: Uses mock data (not API).
* Dependency impact: Uses AdminLayout.
* Risk point: Mock data not real.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/admin/EnrollmentManagePage.vue`
* Change type: ADD
* Key change: Enrollment list with status update.
* Core logic: Uses mock data (not API).
* Dependency impact: Uses AdminLayout.
* Risk point: Mock data not real.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/admin/FinancePage.vue`
* Change type: ADD
* Key change: Finance summary and payment records.
* Core logic: Uses mock data (not API).
* Dependency impact: Uses AdminLayout.
* Risk point: Mock data not real.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

---

## 3. Current verification status (PASS / FAIL / NOT RUN)

* Install: PASS
* Build: PASS
* Typecheck: NOT RUN (no TypeScript)
* Lint: PASS
* Unit tests: PASS (17 tests)
* Integration tests: NOT RUN
* Contract tests: NOT RUN
* Manual verification: NOT RUN
* Current blocker: Frontend not connected to backend API
* Core error: None

---

## 4. Open TODOs and rollback notes

* DONE: JWT secret via environment variable
* Status: COMPLETED
* Target files: `backend/src/main/resources/application.yml`
* Verification: `secret: ${JWT_SECRET:course-manager-jwt-secret-key-2026-must-be-at-least-256-bits}`

* DONE: Database credentials via environment variable
* Status: COMPLETED
* Target files: `backend/src/main/resources/application.yml`
* Verification: `username: ${DB_USERNAME:root}`, `password: ${DB_PASSWORD:root}`

* DONE: Course creation validates categoryId
* Status: COMPLETED
* Target files: `backend/src/main/java/com/institution/coursemanager/service/impl/CourseServiceImpl.java`

* DONE: Course deletion checks associated orders
* Status: COMPLETED
* Target files: `backend/src/main/java/com/institution/coursemanager/service/impl/CourseServiceImpl.java`

* TODO: Connect frontend login to real backend API
* Priority: HIGH
* Required prerequisite: Backend login API working
* Target files: `frontend/src/views/admin/LoginPage.vue`
* Risk: Auth bypass if mock token remains
* Suggested action: Replace mock token with `api/admin.js` login call
* Rollback files: `frontend/src/views/admin/LoginPage.vue`
* Rollback command: `git restore frontend/src/views/admin/LoginPage.vue`
* Post-rollback verification: Manual login test

* TODO: Connect all frontend pages to real backend API
* Priority: HIGH
* Required prerequisite: All backend APIs working
* Target files: All `frontend/src/views/**/*.vue`
* Risk: Mock data shows incorrect information
* Suggested action: Replace mock data with API calls using `api/public.js` and `api/admin.js`
* Rollback files: All frontend view files
* Rollback command: `git restore frontend/src/views/`
* Post-rollback verification: Manual UI test

* TODO: Add logging to AdminAuthInterceptor catch block
* Priority: LOW
* Required prerequisite: None
* Target files: `backend/src/main/java/com/institution/coursemanager/interceptor/AdminAuthInterceptor.java`
* Risk: Cannot debug auth failures
* Suggested action: Add `log.warn("Token validation failed", e)`
* Rollback files: `backend/src/main/java/com/institution/coursemanager/interceptor/AdminAuthInterceptor.java`
* Rollback command: `git restore backend/src/main/java/com/institution/coursemanager/interceptor/AdminAuthInterceptor.java`
* Post-rollback verification: `mvn compile`

* TODO: Phase 9 backend tests (Controller tests)
* Priority: HIGH
* Required prerequisite: All controllers implemented
* Target files: `backend/src/test/java/com/institution/coursemanager/controller/`
* Risk: Untested endpoints
* Suggested action: Create test classes per controller
* Rollback files: Test files only
* Rollback command: `rm -rf backend/src/test/java/com/institution/coursemanager/controller/`
* Post-rollback verification: `mvn test`

* TODO: Phase 10 integration tests (frontend-backend)
* Priority: HIGH
* Required prerequisite: Frontend connected to backend
* Target files: Manual test
* Risk: Untested integration
* Suggested action: Start both servers, test full flow
* Rollback files: N/A
* Rollback command: N/A
* Post-rollback verification: Manual test

---

## 5. Tool outputs (keep PASS / FAIL only)

* Tool: mvn compile
* Command: `cd backend && mvn compile`
* Result: PASS
* Failure reason: None
* Related files: All backend Java files
* Blocking next phase: NO

* Tool: mvn test
* Command: `cd backend && mvn test`
* Result: PASS
* Failure reason: None
* Related files: `EnrollmentServiceImplTest.java`
* Blocking next phase: NO

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

---

## 6. List next phase detailed planning

* Next phase goal: Connect frontend to real backend API (Phase 10 integration)
* Entry point: `frontend/src/views/admin/LoginPage.vue`
* Input prerequisite: Backend server running on port 8080
* Step 1: Modify LoginPage.vue to call `api/admin.js` login function
* Step 2: Remove mock token, store real JWT from response
* Step 3: Modify DashboardPage.vue to call `api/admin.js` overview function
* Step 4: Modify CourseManagePage.vue to call course CRUD APIs
* Step 5: Modify EnrollmentManagePage.vue to call enrollment APIs
* Step 6: Modify FinancePage.vue to call finance APIs
* Step 7: Modify public pages to call `api/public.js` APIs
* Target files: All `frontend/src/views/**/*.vue`
* Expected change: All pages use real API data instead of mock
* Verification method: `cd frontend && pnpm build`, manual UI test
* Acceptance criteria: All pages load data from backend, no mock data
* Risk control: Keep mock data as fallback during development
* Forbidden action: Commit mock data in production code

* Next phase goal: Write backend tests (Phase 9)
* Entry point: `backend/src/test/java/com/institution/coursemanager/`
* Input prerequisite: All controllers implemented
* Step 1: Create `AdminAuthControllerTest.java` (TC-LOGIN-01 ~ TC-LOGIN-08)
* Step 2: Create `AdminCourseControllerTest.java` (TC-COURSE-01 ~ TC-COURSE-14)
* Step 3: Create `PublicCourseControllerTest.java` (TC-COURSE-15 ~ TC-COURSE-19)
* Step 4: Create `PublicEnrollmentControllerTest.java` (TC-ENROLL-01 ~ TC-ENROLL-10)
* Step 5: Create `AdminEnrollmentControllerTest.java` (TC-STATUS-01 ~ TC-STATUS-10)
* Step 6: Create `AdminPaymentControllerTest.java` (TC-PAY-01 ~ TC-PAY-10)
* Step 7: Create `AdminDashboardControllerTest.java` (TC-DASH-01 ~ TC-DASH-07)
* Step 8: Create `AdminFinanceControllerTest.java` (TC-FIN-01 ~ TC-FIN-04)
* Target files: `backend/src/test/java/com/institution/coursemanager/controller/`
* Expected change: All test cases pass
* Verification method: `cd backend && mvn test`
* Acceptance criteria: All tests pass, 0 failures
* Risk control: Use `@SpringBootTest` with test profile
* Forbidden action: Delete tests to make build pass
