package com.institution.coursemanager.controller;

import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.enums.CourseStatus;
import com.institution.coursemanager.enums.EnrollmentStatus;
import com.institution.coursemanager.enums.PaymentStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AdminDashboardController - 管理员仪表盘接口测试")
class AdminDashboardControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("TC-DASH-01: 管理员查看空仪表盘应返回基础统计结构")
    void TC_DASH_01_emptyOverview() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCourses").exists())
                .andExpect(jsonPath("$.data.onlineCourses").exists())
                .andExpect(jsonPath("$.data.totalEnrollments").exists())
                .andExpect(jsonPath("$.data.recentEnrollments").isArray());
    }

    @Test
    @DisplayName("TC-DASH-02: 仪表盘应统计总课程数和上架课程数")
    void TC_DASH_02_courseCounts() throws Exception {
        createCourse("仪表盘上架课程", CourseStatus.ONLINE);
        createCourse("仪表盘草稿课程", CourseStatus.DRAFT);

        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCourses").value(2))
                .andExpect(jsonPath("$.data.onlineCourses").value(1));
    }

    @Test
    @DisplayName("TC-DASH-03: 仪表盘应统计报名总数和待处理报名数")
    void TC_DASH_03_enrollmentCounts() throws Exception {
        Course course = createCourse("报名统计课程", CourseStatus.ONLINE);
        createEnrollmentOrder(course, "待处理学员", PaymentStatus.UNPAID.getCode(), EnrollmentStatus.PENDING.getCode(),
                new BigDecimal("49.00"), BigDecimal.ZERO);
        createEnrollmentOrder(course, "已联系学员", PaymentStatus.UNPAID.getCode(), EnrollmentStatus.CONTACTED.getCode(),
                new BigDecimal("49.00"), BigDecimal.ZERO);

        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalEnrollments").value(2))
                .andExpect(jsonPath("$.data.pendingEnrollments").value(1));
    }

    @Test
    @DisplayName("TC-DASH-04: 仪表盘应统计不同缴费状态数量")
    void TC_DASH_04_paymentStatusCounts() throws Exception {
        Course course = createCourse("缴费状态统计课程", CourseStatus.ONLINE);
        createEnrollmentOrder(course, "已缴费学员", PaymentStatus.PAID.getCode(), EnrollmentStatus.ENROLLED.getCode(),
                new BigDecimal("49.00"), new BigDecimal("49.00"));
        createEnrollmentOrder(course, "未缴费学员", PaymentStatus.UNPAID.getCode(), EnrollmentStatus.PENDING.getCode(),
                new BigDecimal("49.00"), BigDecimal.ZERO);
        createEnrollmentOrder(course, "部分缴费学员", PaymentStatus.PARTIAL.getCode(), EnrollmentStatus.CONTACTED.getCode(),
                new BigDecimal("49.00"), new BigDecimal("20.00"));
        createEnrollmentOrder(course, "已退款学员", PaymentStatus.REFUNDED.getCode(), EnrollmentStatus.CANCELLED.getCode(),
                new BigDecimal("49.00"), BigDecimal.ZERO);

        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paidCount").value(1))
                .andExpect(jsonPath("$.data.unpaidCount").value(1))
                .andExpect(jsonPath("$.data.partialCount").value(1))
                .andExpect(jsonPath("$.data.refundedCount").value(1));
    }

    @Test
    @DisplayName("TC-DASH-05: 仪表盘应返回最近报名列表")
    void TC_DASH_05_recentEnrollments() throws Exception {
        Course course = createCourse("最近报名课程", CourseStatus.ONLINE);
        createPendingUnpaidOrder(course, "最近报名学员");

        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.recentEnrollments[0].orderNo").isNotEmpty())
                .andExpect(jsonPath("$.data.recentEnrollments[0].courseTitle").value("最近报名课程"))
                .andExpect(jsonPath("$.data.recentEnrollments[0].studentName").value("最近报名学员"));
    }

    @Test
    @DisplayName("TC-DASH-06: 未登录访问仪表盘应返回未授权")
    void TC_DASH_06_unauthorizedOverview() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/overview"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("TC-DASH-07: Token 无效访问仪表盘应返回未授权")
    void TC_DASH_07_invalidTokenOverview() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
