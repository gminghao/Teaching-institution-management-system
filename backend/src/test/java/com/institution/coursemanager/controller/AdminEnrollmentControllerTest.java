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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AdminEnrollmentController - 管理员报名状态接口测试")
class AdminEnrollmentControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("TC-STATUS-01: 管理员查询报名列表应返回分页结构")
    void TC_STATUS_01_listEnrollments() throws Exception {
        Course course = createCourse("报名列表课程", CourseStatus.ONLINE);
        createPendingUnpaidOrder(course, "张三");

        mockMvc.perform(get("/api/admin/enrollments")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("TC-STATUS-02: 管理员按关键字筛选报名应返回匹配记录")
    void TC_STATUS_02_filterEnrollmentsByKeyword() throws Exception {
        Course course = createCourse("关键字报名课程", CourseStatus.ONLINE);
        createPendingUnpaidOrder(course, "李四");

        mockMvc.perform(get("/api/admin/enrollments")
                        .param("keyword", "李四")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].studentName").value("李四"));
    }

    @Test
    @DisplayName("TC-STATUS-03: 管理员按缴费状态筛选报名应返回匹配记录")
    void TC_STATUS_03_filterEnrollmentsByPaymentStatus() throws Exception {
        Course course = createCourse("缴费筛选课程", CourseStatus.ONLINE);
        createEnrollmentOrder(course, "王五", PaymentStatus.PAID.getCode(), EnrollmentStatus.ENROLLED.getCode(),
                new BigDecimal("49.00"), new BigDecimal("49.00"));

        mockMvc.perform(get("/api/admin/enrollments")
                        .param("paymentStatus", "PAID")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].paymentStatus").value("PAID"));
    }

    @Test
    @DisplayName("TC-STATUS-04: 管理员按报名状态筛选报名应返回匹配记录")
    void TC_STATUS_04_filterEnrollmentsByEnrollmentStatus() throws Exception {
        Course course = createCourse("状态筛选课程", CourseStatus.ONLINE);
        createEnrollmentOrder(course, "赵六", PaymentStatus.UNPAID.getCode(), EnrollmentStatus.CONTACTED.getCode(),
                new BigDecimal("49.00"), BigDecimal.ZERO);

        mockMvc.perform(get("/api/admin/enrollments")
                        .param("enrollmentStatus", "CONTACTED")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].enrollmentStatus").value("CONTACTED"));
    }

    @Test
    @DisplayName("TC-STATUS-05: 待处理报名变更为已联系应返回成功")
    void TC_STATUS_05_pendingToContacted() throws Exception {
        EnrollmentOrder order = createPendingOrder("待联系学员");

        mockMvc.perform(put("/api/admin/enrollments/{id}/status", order.getId())
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enrollmentStatus\":\"CONTACTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-STATUS-06: 待处理报名变更为已取消应返回成功")
    void TC_STATUS_06_pendingToCancelled() throws Exception {
        EnrollmentOrder order = createPendingOrder("待取消学员");

        mockMvc.perform(put("/api/admin/enrollments/{id}/status", order.getId())
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enrollmentStatus\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-STATUS-07: 已联系报名变更为已录取应返回成功")
    void TC_STATUS_07_contactedToEnrolled() throws Exception {
        EnrollmentOrder order = createContactedOrder("待录取学员");

        mockMvc.perform(put("/api/admin/enrollments/{id}/status", order.getId())
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enrollmentStatus\":\"ENROLLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-STATUS-08: 已联系报名变更为已取消应返回成功")
    void TC_STATUS_08_contactedToCancelled() throws Exception {
        EnrollmentOrder order = createContactedOrder("联系后取消学员");

        mockMvc.perform(put("/api/admin/enrollments/{id}/status", order.getId())
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enrollmentStatus\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-STATUS-09: 报名状态值无效应返回参数错误")
    void TC_STATUS_09_invalidEnrollmentStatus() throws Exception {
        EnrollmentOrder order = createPendingOrder("状态无效学员");

        mockMvc.perform(put("/api/admin/enrollments/{id}/status", order.getId())
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enrollmentStatus\":\"UNKNOWN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-STATUS-10: 待处理报名直接变更为已录取应返回冲突")
    void TC_STATUS_10_invalidStatusTransition() throws Exception {
        EnrollmentOrder order = createPendingOrder("非法流转学员");

        mockMvc.perform(put("/api/admin/enrollments/{id}/status", order.getId())
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enrollmentStatus\":\"ENROLLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));
    }

    private EnrollmentOrder createPendingOrder(String studentName) {
        Course course = createCourse("状态流转课程-" + studentName, CourseStatus.ONLINE);
        return createPendingUnpaidOrder(course, studentName);
    }

    private EnrollmentOrder createContactedOrder(String studentName) {
        Course course = createCourse("状态流转课程-" + studentName, CourseStatus.ONLINE);
        return createEnrollmentOrder(course, studentName, PaymentStatus.UNPAID.getCode(),
                EnrollmentStatus.CONTACTED.getCode(), course.getRegistrationFee(), BigDecimal.ZERO);
    }
}
