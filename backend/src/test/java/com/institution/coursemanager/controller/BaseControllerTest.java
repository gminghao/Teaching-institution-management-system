package com.institution.coursemanager.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.institution.coursemanager.entity.AdminUser;
import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.entity.CourseCategory;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.enums.CourseStatus;
import com.institution.coursemanager.enums.EnrollmentStatus;
import com.institution.coursemanager.enums.PaymentStatus;
import com.institution.coursemanager.mapper.AdminUserMapper;
import com.institution.coursemanager.mapper.CourseCategoryMapper;
import com.institution.coursemanager.mapper.CourseMapper;
import com.institution.coursemanager.mapper.EnrollmentOrderMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller 测试基类
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected CourseMapper courseMapper;

    @Autowired
    protected EnrollmentOrderMapper enrollmentOrderMapper;

    @Autowired
    protected AdminUserMapper adminUserMapper;

    @Autowired
    protected CourseCategoryMapper courseCategoryMapper;

    private boolean dataInitialized = false;

    @BeforeEach
    void initBaseData() {
        if (dataInitialized) {
            return;
        }
        // 初始化管理员用户
        if (adminUserMapper.selectCount(null) == 0) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setStatus(1);
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            adminUserMapper.insert(admin);
        }
        // 初始化课程分类
        if (courseCategoryMapper.selectCount(null) == 0) {
            CourseCategory category = new CourseCategory();
            category.setName("编程开发");
            category.setSortOrder(1);
            category.setStatus(1);
            category.setCreateTime(LocalDateTime.now());
            category.setUpdateTime(LocalDateTime.now());
            courseCategoryMapper.insert(category);
        }
        dataInitialized = true;
    }

    /**
     * 获取管理员 JWT Token
     */
    protected String getAdminToken() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("token").asText();
    }

    /**
     * 获取带 Authorization header 的管理员请求头值
     */
    protected String bearerToken(String token) {
        return "Bearer " + token;
    }

    protected Course createCourse(String title, CourseStatus status) {
        Course course = new Course();
        course.setCategoryId(1L);
        course.setTitle(title);
        course.setSubtitle(title + " subtitle");
        course.setCoverImage("https://example.com/course.png");
        course.setDescription(title + " description");
        course.setTeacherName("Teacher Wang");
        course.setPrice(new BigDecimal("199.00"));
        course.setRegistrationFee(new BigDecimal("49.00"));
        course.setStatus(status.getCode());
        course.setDeleted(0);
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.insert(course);
        return course;
    }

    protected EnrollmentOrder createEnrollmentOrder(
            Course course,
            String studentName,
            String paymentStatus,
            String enrollmentStatus,
            BigDecimal registrationFee,
            BigDecimal paidAmount) {
        EnrollmentOrder order = new EnrollmentOrder();
        order.setOrderNo("EN" + System.nanoTime());
        order.setCourseId(course.getId());
        order.setCourseTitle(course.getTitle());
        order.setStudentName(studentName);
        order.setStudentPhone("13800138000");
        order.setStudentEmail("student@example.com");
        order.setRemark("test enrollment");
        order.setRegistrationFee(registrationFee);
        order.setPaidAmount(paidAmount);
        order.setPaymentStatus(paymentStatus);
        order.setEnrollmentStatus(enrollmentStatus);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        enrollmentOrderMapper.insert(order);
        return order;
    }

    protected EnrollmentOrder createPendingUnpaidOrder(Course course, String studentName) {
        return createEnrollmentOrder(
                course,
                studentName,
                PaymentStatus.UNPAID.getCode(),
                EnrollmentStatus.PENDING.getCode(),
                course.getRegistrationFee(),
                BigDecimal.ZERO);
    }
}
