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
@DisplayName("AdminFinanceController - 管理员财务统计接口测试")
class AdminFinanceControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("TC-FIN-01: 管理员查看空财务汇总应返回零值结构")
    void TC_FIN_01_emptyFinanceSummary() throws Exception {
        mockMvc.perform(get("/api/admin/finance/summary")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderCount").value(0))
                .andExpect(jsonPath("$.data.totalRegistrationFee").exists())
                .andExpect(jsonPath("$.data.totalPaidAmount").exists())
                .andExpect(jsonPath("$.data.totalUnpaidAmount").exists());
    }

    @Test
    @DisplayName("TC-FIN-02: 管理员查看财务汇总应返回订单和缴费状态统计")
    void TC_FIN_02_financeSummaryWithOrders() throws Exception {
        Course course = createCourse("财务统计课程", CourseStatus.ONLINE);
        createEnrollmentOrder(course, "已缴费财务学员", PaymentStatus.PAID.getCode(), EnrollmentStatus.ENROLLED.getCode(),
                new BigDecimal("100.00"), new BigDecimal("100.00"));
        createEnrollmentOrder(course, "未缴费财务学员", PaymentStatus.UNPAID.getCode(), EnrollmentStatus.PENDING.getCode(),
                new BigDecimal("80.00"), BigDecimal.ZERO);
        createEnrollmentOrder(course, "部分缴费财务学员", PaymentStatus.PARTIAL.getCode(), EnrollmentStatus.CONTACTED.getCode(),
                new BigDecimal("60.00"), new BigDecimal("20.00"));
        createEnrollmentOrder(course, "已退款财务学员", PaymentStatus.REFUNDED.getCode(), EnrollmentStatus.CANCELLED.getCode(),
                new BigDecimal("40.00"), new BigDecimal("10.00"));

        mockMvc.perform(get("/api/admin/finance/summary")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderCount").value(4))
                .andExpect(jsonPath("$.data.paidCount").value(1))
                .andExpect(jsonPath("$.data.unpaidCount").value(1))
                .andExpect(jsonPath("$.data.partialCount").value(1))
                .andExpect(jsonPath("$.data.refundedCount").value(1))
                .andExpect(jsonPath("$.data.totalRegistrationFee").value(280.00))
                .andExpect(jsonPath("$.data.totalPaidAmount").value(130.00));
    }

    @Test
    @DisplayName("TC-FIN-03: 未登录访问财务汇总应返回未授权")
    void TC_FIN_03_unauthorizedFinanceSummary() throws Exception {
        mockMvc.perform(get("/api/admin/finance/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("TC-FIN-04: Token 无效访问财务汇总应返回未授权")
    void TC_FIN_04_invalidTokenFinanceSummary() throws Exception {
        mockMvc.perform(get("/api/admin/finance/summary")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
