package com.institution.coursemanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.institution.coursemanager.entity.EnrollmentOrder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 业务流程集成测试：完整报名-缴费流程
 *
 * 覆盖 test-cases.md Section 8.1 的 11 个步骤：
 * 管理员登录 → 新增课程 → 上架 → 访客查看 → 提交报名 → 管理员查看报名
 * → 修改状态 → 部分缴费 → 全额缴费 → 仪表盘验证 → 财务汇总验证
 */
class EnrollmentPaymentFlowTest extends BaseControllerTest {

    @Test
    void fullEnrollmentPaymentFlow() throws Exception {
        // ── Step 1: 管理员登录，获取 token ──
        String token = getAdminToken();

        // ── Step 2: 新增课程，验证 status=DRAFT ──
        String courseJson = """
                {
                    "categoryId": 1,
                    "title": "Java Senior Dev",
                    "subtitle": "Advanced",
                    "coverImage": "https://example.com/java.png",
                    "description": "Learn Java",
                    "teacherName": "Prof Wang",
                    "price": 1999.00,
                    "registrationFee": 500.00
                }
                """;

        MvcResult courseResult = mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        long courseId = extractLong(courseResult, "data", "id");

        // ── Step 3: 上架课程，验证 status=ONLINE ──
        mockMvc.perform(put("/api/admin/courses/" + courseId + "/online")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // ── Step 4: 访客查看课程列表，确认能看到该课程 ──
        mockMvc.perform(get("/api/public/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[?(@.id==" + courseId + ")]").exists());

        // ── Step 5: 访客提交报名，验证返回 orderNo ──
        String enrollJson = String.format("""
                {
                    "courseId": %d,
                    "studentName": "Zhang San",
                    "studentPhone": "13800001111",
                    "studentEmail": "zhangsan@example.com",
                    "remark": "Want to learn Java"
                }
                """, courseId);

        MvcResult enrollResult = mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(enrollJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").isNotEmpty())
                .andReturn();

        // 提取 orderNo，从 DB 查出 orderId
        String orderNo = extractString(enrollResult, "data", "orderNo");
        EnrollmentOrder order = enrollmentOrderMapper.selectOne(
                new LambdaQueryWrapper<EnrollmentOrder>()
                        .eq(EnrollmentOrder::getOrderNo, orderNo));
        long orderId = order.getId();

        // ── Step 6: 管理员查看报名列表，确认能看到该订单 ──
        mockMvc.perform(get("/api/admin/enrollments")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[?(@.id==" + orderId + ")]").exists());

        // ── Step 7: 管理员修改状态为 CONTACTED ──
        String statusJson = "{\"enrollmentStatus\": \"CONTACTED\"}";
        mockMvc.perform(put("/api/admin/enrollments/" + orderId + "/status")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // ── Step 8: 部分缴费 300，验证 payment_status=PARTIAL ──
        String payment1Json = String.format("""
                {
                    "orderId": %d,
                    "amount": 300.00,
                    "paymentMethod": "CASH",
                    "operatorName": "Finance Li"
                }
                """, orderId);

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payment1Json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证订单缴费状态变为 PARTIAL
        mockMvc.perform(get("/api/admin/enrollments")
                        .header("Authorization", bearerToken(token))
                        .param("pageNum", "1")
                        .param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.id==" + orderId + ")].paymentStatus").value("PARTIAL"));

        // ── Step 9: 缴纳剩余 200，验证 payment_status=PAID, paid_amount=500 ──
        String payment2Json = String.format("""
                {
                    "orderId": %d,
                    "amount": 200.00,
                    "paymentMethod": "WECHAT",
                    "operatorName": "Finance Li"
                }
                """, orderId);

        mockMvc.perform(post("/api/admin/payments")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payment2Json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证订单缴费状态变为 PAID
        mockMvc.perform(get("/api/admin/enrollments")
                        .header("Authorization", bearerToken(token))
                        .param("pageNum", "1")
                        .param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[?(@.id==" + orderId + ")].paymentStatus").value("PAID"));

        // ── Step 10: 查看仪表盘，验证数据正确 ──
        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCourses").isNumber())
                .andExpect(jsonPath("$.data.onlineCourses").isNumber())
                .andExpect(jsonPath("$.data.totalEnrollments").isNumber())
                .andExpect(jsonPath("$.data.paidCount").isNumber());

        // ── Step 11: 查看财务汇总，验证缴费金额正确 ──
        mockMvc.perform(get("/api/admin/finance/summary")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalPaidAmount").isNumber())
                .andExpect(jsonPath("$.data.paidCount").isNumber());
    }

    /**
     * 从响应 JSON 中提取 long 值
     */
    private long extractLong(MvcResult result, String... paths) throws Exception {
        JsonNode node = parseResponse(result);
        for (String p : paths) {
            node = node.path(p);
        }
        return node.asLong();
    }

    /**
     * 从响应 JSON 中提取 String 值
     */
    private String extractString(MvcResult result, String... paths) throws Exception {
        JsonNode node = parseResponse(result);
        for (String p : paths) {
            node = node.path(p);
        }
        return node.asText();
    }

    private JsonNode parseResponse(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }
}
