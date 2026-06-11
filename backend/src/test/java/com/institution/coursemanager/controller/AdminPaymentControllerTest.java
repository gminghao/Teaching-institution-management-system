package com.institution.coursemanager.controller;

import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.enums.CourseStatus;
import com.institution.coursemanager.enums.EnrollmentStatus;
import com.institution.coursemanager.enums.PaymentStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AdminPaymentController - 管理员缴费接口测试")
class AdminPaymentControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("TC-PAY-01: 管理员创建现金缴费记录应返回缴费信息")
    void TC_PAY_01_createCashPayment() throws Exception {
        EnrollmentOrder order = createUnpaidOrder("现金缴费学员");
        String json = paymentJson(order.getId(), "49.00", "CASH", null);

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.orderId").value(order.getId()))
                .andExpect(jsonPath("$.data.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.data.paymentTime").isNotEmpty());
    }

    @Test
    @DisplayName("TC-PAY-02: 管理员创建微信部分缴费记录应返回缴费金额")
    void TC_PAY_02_createPartialWechatPayment() throws Exception {
        EnrollmentOrder order = createUnpaidOrder("微信部分缴费学员");
        String json = paymentJson(order.getId(), "20.00", "WECHAT", null);

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.amount").value(20.00))
                .andExpect(jsonPath("$.data.paymentMethod").value("WECHAT"));
    }

    @Test
    @DisplayName("TC-PAY-03: 管理员使用标准日期时间创建缴费记录应返回指定时间")
    void TC_PAY_03_createPaymentWithFormattedTime() throws Exception {
        EnrollmentOrder order = createUnpaidOrder("指定时间缴费学员");
        String json = paymentJson(order.getId(), "49.00", "ALIPAY", "2026-06-01 10:30:00");

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paymentMethod").value("ALIPAY"))
                .andExpect(jsonPath("$.data.paymentTime").value("2026-06-01 10:30:00"));
    }

    @Test
    @DisplayName("TC-PAY-04: 管理员使用ISO时间创建缴费记录应返回格式化时间")
    void TC_PAY_04_createPaymentWithIsoTime() throws Exception {
        EnrollmentOrder order = createUnpaidOrder("ISO时间缴费学员");
        String json = paymentJson(order.getId(), "49.00", "BANK", "2026-06-01T10:30:00");

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paymentMethod").value("BANK"))
                .andExpect(jsonPath("$.data.paymentTime").value("2026-06-01 10:30:00"));
    }

    @Test
    @DisplayName("TC-PAY-05: 缴费缺少订单ID应返回参数错误")
    void TC_PAY_05_missingOrderId() throws Exception {
        String json = """
                {
                  "amount": 49.00,
                  "paymentMethod": "CASH"
                }
                """;

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-PAY-06: 缴费金额为空应返回参数错误")
    void TC_PAY_06_missingAmount() throws Exception {
        EnrollmentOrder order = createUnpaidOrder("金额为空学员");
        String json = """
                {
                  "orderId": %d,
                  "paymentMethod": "CASH"
                }
                """.formatted(order.getId());

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-PAY-07: 缴费金额为零应返回参数错误")
    void TC_PAY_07_zeroAmount() throws Exception {
        EnrollmentOrder order = createUnpaidOrder("零金额学员");
        String json = paymentJson(order.getId(), "0.00", "CASH", null);

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-PAY-08: 缴费方式无效应返回参数错误")
    void TC_PAY_08_invalidPaymentMethod() throws Exception {
        EnrollmentOrder order = createUnpaidOrder("方式无效学员");
        String json = paymentJson(order.getId(), "49.00", "CARD", null);

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-PAY-09: 缴费订单不存在应返回未找到")
    void TC_PAY_09_orderNotFound() throws Exception {
        String json = paymentJson(9999L, "49.00", "CASH", null);

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("TC-PAY-10: 缴费金额超过报名费应返回冲突")
    void TC_PAY_10_amountExceedsRegistrationFee() throws Exception {
        EnrollmentOrder order = createUnpaidOrder("超额缴费学员");
        String json = paymentJson(order.getId(), "50.00", "CASH", null);

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));
    }

    private EnrollmentOrder createUnpaidOrder(String studentName) {
        Course course = createCourse("缴费课程-" + studentName, CourseStatus.ONLINE);
        return createEnrollmentOrder(course, studentName, PaymentStatus.UNPAID.getCode(),
                EnrollmentStatus.PENDING.getCode(), new BigDecimal("49.00"), BigDecimal.ZERO);
    }

    private String paymentJson(Long orderId, String amount, String paymentMethod, String paymentTime) {
        String timeField = paymentTime == null ? "" : ",\n  \"paymentTime\": \"" + paymentTime + "\"";
        return """
                {
                  "orderId": %d,
                  "amount": %s,
                  "paymentMethod": "%s",
                  "operatorName": "admin",
                  "remark": "线下确认"%s
                }
                """.formatted(orderId, amount, paymentMethod, timeField);
    }
}
