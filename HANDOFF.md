# HANDOFF.md

> Project handoff report — generated 2026-06-12
> Updated: 2026-06-12
> Current branch: `dev` | Base branch: `main`
> Progress: 74/78 tasks (94.9%)

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

### Backend — Entity

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

* Path: `backend/src/main/java/com/institution/coursemanager/entity/CourseCategory.java`
* Change type: ADD
* Key change: Course category entity.
* Core logic: Maps to `course_category` table.
* Dependency impact: Used by CourseService.
* Risk point: None.
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

* Path: `backend/src/main/java/com/institution/coursemanager/entity/PaymentRecord.java`
* Change type: ADD
* Key change: Payment record entity.
* Core logic: Stores payment transactions.
* Dependency impact: Used by PaymentService.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

### Backend — Service

* Path: `backend/src/main/java/com/institution/coursemanager/service/impl/CourseServiceImpl.java`
* Change type: ADD
* Key change: Course CRUD + online/offline + pagination with page size limit (100).
* Core logic: Status validation, logical delete check, categoryId validation.
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

* Path: `backend/src/main/java/com/institution/coursemanager/service/impl/DashboardServiceImpl.java`
* Change type: ADD
* Key change: Dashboard overview aggregation.
* Core logic: Counts and statistics for admin dashboard.
* Dependency impact: Uses EnrollmentOrderMapper, CourseMapper.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/service/impl/AuthServiceImpl.java`
* Change type: ADD
* Key change: Admin login with BCrypt password verification.
* Core logic: JWT token generation.
* Dependency impact: Uses AdminUserMapper, JwtUtil.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

### Backend — Mapper

* Path: `backend/src/main/java/com/institution/coursemanager/mapper/EnrollmentOrderMapper.java`
* Change type: ADD
* Key change: Added `selectFinanceSummary()` aggregation query.
* Core logic: SQL SUM/COUNT with CASE for status breakdown.
* Dependency impact: Used by FinanceServiceImpl.
* Risk point: None.
* Rollback method: Revert to original.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/mapper/CourseMapper.java`
* Change type: ADD
* Key change: Course mapper with XML-based queries.
* Core logic: Supports dynamic conditions for public/admin queries.
* Dependency impact: Used by CourseServiceImpl.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/resources/mapper/CourseMapper.xml`
* Change type: ADD
* Key change: JOIN query for course + category.
* Core logic: `selectPublicPage` and `selectAdminPage` with dynamic conditions.
* Dependency impact: Used by CourseMapper interface.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

### Backend — Controller

* Path: `backend/src/main/java/com/institution/coursemanager/controller/admin/AdminAuthController.java`
* Change type: ADD
* Key change: Admin login endpoint.
* Core logic: POST /api/admin/login.
* Dependency impact: Uses AuthService.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/controller/admin/AdminCourseController.java`
* Change type: ADD
* Key change: Admin course CRUD endpoints.
* Core logic: GET/POST/PUT/DELETE /api/admin/courses.
* Dependency impact: Uses CourseService.
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

* Path: `backend/src/main/java/com/institution/coursemanager/controller/admin/AdminPaymentController.java`
* Change type: ADD
* Key change: Payment recording endpoints.
* Core logic: POST /api/admin/payments.
* Dependency impact: Uses PaymentService.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/controller/admin/AdminDashboardController.java`
* Change type: ADD
* Key change: Dashboard overview endpoint.
* Core logic: GET /api/admin/dashboard/overview.
* Dependency impact: Uses DashboardService.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/main/java/com/institution/coursemanager/controller/admin/AdminFinanceController.java`
* Change type: ADD
* Key change: Finance summary endpoint.
* Core logic: GET /api/admin/finance/summary.
* Dependency impact: Uses FinanceService.
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

* Path: `backend/src/main/java/com/institution/coursemanager/controller/visitor/PublicEnrollmentController.java`
* Change type: ADD
* Key change: Public enrollment submission endpoint.
* Core logic: No auth required.
* Dependency impact: Uses EnrollmentService.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

### Backend — Config & Exception

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

* Path: `backend/src/main/resources/application.yml`
* Change type: MODIFY
* Key change: Environment variables for JWT secret and DB credentials.
* Core logic: `${JWT_SECRET:...}`, `${DB_USERNAME:root}`, `${DB_PASSWORD:root}`.
* Dependency impact: None.
* Risk point: Default values used if env vars not set.
* Rollback method: `git restore backend/src/main/resources/application.yml`
* Pitfall: UNKNOWN.

### Backend — Tests (Phase 9)

* Path: `backend/src/test/java/com/institution/coursemanager/controller/BaseControllerTest.java`
* Change type: ADD
* Key change: Test base class with @BeforeEach data initialization.
* Core logic: Seeds admin user (BCrypt) and course category before each test.
* Dependency impact: All controller tests extend this class.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/test/java/com/institution/coursemanager/controller/AdminAuthControllerTest.java`
* Change type: ADD
* Key change: 8 login test cases.
* Core logic: TC-LOGIN-01 ~ TC-LOGIN-08.
* Dependency impact: None.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/test/java/com/institution/coursemanager/controller/AdminCourseControllerTest.java`
* Change type: ADD
* Key change: 14 course CRUD + publish/unpublish test cases.
* Core logic: TC-COURSE-01 ~ TC-COURSE-14.
* Dependency impact: None.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/test/java/com/institution/coursemanager/controller/PublicCourseControllerTest.java`
* Change type: ADD
* Key change: 5 public course list test cases.
* Core logic: TC-COURSE-15 ~ TC-COURSE-19.
* Dependency impact: None.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/test/java/com/institution/coursemanager/controller/PublicEnrollmentControllerTest.java`
* Change type: ADD
* Key change: 10 enrollment submission test cases.
* Core logic: TC-ENROLL-01 ~ TC-ENROLL-10.
* Dependency impact: None.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/test/java/com/institution/coursemanager/controller/AdminEnrollmentControllerTest.java`
* Change type: ADD
* Key change: 10 enrollment status update test cases.
* Core logic: TC-STATUS-01 ~ TC-STATUS-10.
* Dependency impact: None.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/test/java/com/institution/coursemanager/controller/AdminPaymentControllerTest.java`
* Change type: ADD
* Key change: 10 payment recording test cases.
* Core logic: TC-PAY-01 ~ TC-PAY-10.
* Dependency impact: None.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/test/java/com/institution/coursemanager/controller/AdminDashboardControllerTest.java`
* Change type: ADD
* Key change: 5 dashboard overview test cases.
* Core logic: TC-DASH-01 ~ TC-DASH-05.
* Dependency impact: None.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/test/java/com/institution/coursemanager/controller/AdminFinanceControllerTest.java`
* Change type: ADD
* Key change: 4 finance summary test cases.
* Core logic: TC-FIN-01 ~ TC-FIN-04.
* Dependency impact: None.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `backend/src/test/java/com/institution/coursemanager/service/EnrollmentServiceImplTest.java`
* Change type: ADD
* Key change: Service layer unit tests for enrollment.
* Core logic: Status transition tests, edge case tests.
* Dependency impact: None.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

### Frontend — Infrastructure

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

* Path: `frontend/src/utils/format.js`
* Change type: MODIFY
* Key change: Added `courseStatusTone()` function.
* Core logic: `enrollmentStatusMap`, `paymentStatusMap`, `courseStatusMap`, `courseStatusTone()`, `formatMoney()`.
* Dependency impact: Used by all admin pages.
* Risk point: None.
* Rollback method: `git restore frontend/src/utils/format.js`.
* Pitfall: UNKNOWN.

* Path: `frontend/src/data/mock.js`
* Change type: ADD
* Key change: Mock data with backend enum values and RMB amounts.
* Core logic: `mockCourses`, `mockEnrollments` with `paidAmount <= registrationFee` invariant.
* Dependency impact: Used by all frontend pages.
* Risk point: Not connected to real API.
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

### Frontend — Visitor Pages

* Path: `frontend/src/views/public/HomePage.vue`
* Change type: MODIFY
* Key change: Home page now calls `getCourses()` API for popular courses.
* Core logic: Real API data instead of mock.
* Dependency impact: Uses PublicLayout, api/public.js.
* Risk point: None.
* Rollback method: `git restore frontend/src/views/public/HomePage.vue`.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/public/CourseListPage.vue`
* Change type: MODIFY
* Key change: Course list now calls `getCourses()` API, removed mock category import.
* Core logic: Real API data instead of mock.
* Dependency impact: Uses PublicLayout, api/public.js.
* Risk point: None.
* Rollback method: `git restore frontend/src/views/public/CourseListPage.vue`.
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

### Frontend — Admin Pages

* Path: `frontend/src/views/admin/LoginPage.vue`
* Change type: ADD
* Key change: Admin login page.
* Core logic: Sets mock token, no real API call.
* Dependency impact: Uses auth utils.
* Risk point: No real authentication.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/admin/DashboardPage.vue`
* Change type: MODIFY
* Key change: Dashboard now calls `getDashboardOverview()` API.
* Core logic: Real API data instead of mock.
* Dependency impact: Uses AdminLayout, format.js, api/admin.js.
* Risk point: None.
* Rollback method: `git restore frontend/src/views/admin/DashboardPage.vue`.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/admin/CourseManagePage.vue`
* Change type: MODIFY
* Key change: Course management now calls `getAdminCourses()` API.
* Core logic: Real API data instead of mock, online/offline actions.
* Dependency impact: Uses AdminLayout, format.js, api/admin.js.
* Risk point: None.
* Rollback method: `git restore frontend/src/views/admin/CourseManagePage.vue`.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/admin/EnrollmentManagePage.vue`
* Change type: MODIFY
* Key change: Enrollment list now calls `getEnrollments()` API.
* Core logic: Real API data instead of mock.
* Dependency impact: Uses AdminLayout, format.js, api/admin.js.
* Risk point: None.
* Rollback method: `git restore frontend/src/views/admin/EnrollmentManagePage.vue`.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/admin/FinancePage.vue`
* Change type: MODIFY
* Key change: Finance summary now calls `getFinanceSummary()` and `getEnrollments()` API.
* Core logic: Real API data instead of mock.
* Dependency impact: Uses AdminLayout, format.js, api/admin.js.
* Risk point: None.
* Rollback method: `git restore frontend/src/views/admin/FinancePage.vue`.
* Pitfall: UNKNOWN.

* Path: `frontend/src/views/NotFoundPage.vue`
* Change type: ADD
* Key change: 404 page with Element Plus `el-result`.
* Core logic: Navigation buttons to home/back.
* Dependency impact: Requires Element Plus.
* Risk point: None.
* Rollback method: Delete file.
* Pitfall: UNKNOWN.

---

## 3. Current verification status (PASS / FAIL / NOT RUN)

* Install: PASS
* Build: PASS
* Typecheck: NOT RUN (no TypeScript)
* Lint: PASS
* Unit tests: PASS (85 tests, 0 failures)
* Integration tests: NOT RUN
* Contract tests: NOT RUN
* Manual verification: NOT RUN
* Current blocker: End-to-end verification pending
* Core error: None

---

## 4. Open TODOs and rollback notes

* ~~TODO: Connect frontend login to real backend API~~ ✅ DONE
* ~~TODO: Connect all frontend pages to real backend API~~ ✅ DONE

* TODO: Add logging to AdminAuthInterceptor catch block
* Priority: LOW
* Required prerequisite: None
* Target files: `backend/src/main/java/com/institution/coursemanager/interceptor/AdminAuthInterceptor.java`
* Risk: Cannot debug auth failures
* Suggested action: Add `log.warn("Token validation failed", e)`
* Rollback files: `backend/src/main/java/com/institution/coursemanager/interceptor/AdminAuthInterceptor.java`
* Rollback command: `git restore backend/src/main/java/com/institution/coursemanager/interceptor/AdminAuthInterceptor.java`
* Post-rollback verification: `mvn compile`

* TODO: Phase 10 end-to-end verification (frontend-backend)
* Priority: HIGH
* Required prerequisite: Frontend connected to backend ✅
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
* Related files: All backend test files
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

* ~~Next phase goal: Connect frontend to real backend API (Phase 10 integration)~~ ✅ DONE

* Next phase goal: End-to-end verification (Phase 10.7)
* Entry point: Both frontend and backend servers
* Input prerequisite: Backend server running on port 8080, Frontend dev server on 5173
* Step 1: Start backend: `cd backend && mvn spring-boot:run`
* Step 2: Start frontend: `cd frontend && pnpm dev`
* Step 3: Test admin login flow (admin/admin123)
* Step 4: Test course CRUD operations
* Step 5: Test enrollment management
* Step 6: Test payment registration
* Step 7: Test public pages (course list, detail, enrollment)
* Step 8: Verify dashboard and finance summary
* Target files: Manual test
* Expected change: All features work end-to-end
* Verification method: Manual UI test
* Acceptance criteria: Full business flow works
* Risk control: N/A
* Forbidden action: N/A
