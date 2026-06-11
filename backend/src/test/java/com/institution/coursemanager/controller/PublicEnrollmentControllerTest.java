package com.institution.coursemanager.controller;

import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.enums.CourseStatus;
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
@DisplayName("PublicEnrollmentController - 访客报名接口测试")
class PublicEnrollmentControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("TC-ENROLL-01: 访客提交有效报名应返回订单信息")
    void TC_ENROLL_01_submitEnrollment() throws Exception {
        Course course = createCourse("报名成功课程", CourseStatus.ONLINE);
        String json = """
                {
                  "courseId": %d,
                  "studentName": "张三",
                  "studentPhone": "13800138000",
                  "studentEmail": "zhangsan@example.com",
                  "remark": "希望周末上课"
                }
                """.formatted(course.getId());

        mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").isNotEmpty())
                .andExpect(jsonPath("$.data.courseTitle").value("报名成功课程"))
                .andExpect(jsonPath("$.data.registrationFee").exists())
                .andExpect(jsonPath("$.data.message").isNotEmpty());
    }

    @Test
    @DisplayName("TC-ENROLL-02: 报名缺少课程ID应返回参数错误")
    void TC_ENROLL_02_missingCourseId() throws Exception {
        String json = """
                {
                  "studentName": "李四",
                  "studentPhone": "13800138000"
                }
                """;

        mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-ENROLL-03: 报名未上架课程应返回未找到")
    void TC_ENROLL_03_offlineCourse() throws Exception {
        Course course = createCourse("不可报名课程", CourseStatus.OFFLINE);
        String json = validEnrollmentJson(course.getId());

        mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("TC-ENROLL-04: 报名不存在课程应返回未找到")
    void TC_ENROLL_04_courseNotFound() throws Exception {
        String json = validEnrollmentJson(9999L);

        mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("TC-ENROLL-05: 报名姓名为空应返回参数错误")
    void TC_ENROLL_05_emptyStudentName() throws Exception {
        Course course = createCourse("姓名校验课程", CourseStatus.ONLINE);
        String json = """
                {
                  "courseId": %d,
                  "studentName": "",
                  "studentPhone": "13800138000"
                }
                """.formatted(course.getId());

        mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-ENROLL-06: 报名姓名长度不足应返回参数错误")
    void TC_ENROLL_06_shortStudentName() throws Exception {
        Course course = createCourse("姓名长度课程", CourseStatus.ONLINE);
        String json = """
                {
                  "courseId": %d,
                  "studentName": "王",
                  "studentPhone": "13800138000"
                }
                """.formatted(course.getId());

        mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-ENROLL-07: 报名手机号为空应返回参数错误")
    void TC_ENROLL_07_emptyStudentPhone() throws Exception {
        Course course = createCourse("手机号为空课程", CourseStatus.ONLINE);
        String json = """
                {
                  "courseId": %d,
                  "studentName": "赵六",
                  "studentPhone": ""
                }
                """.formatted(course.getId());

        mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-ENROLL-08: 报名手机号格式错误应返回参数错误")
    void TC_ENROLL_08_invalidStudentPhone() throws Exception {
        Course course = createCourse("手机号格式课程", CourseStatus.ONLINE);
        String json = """
                {
                  "courseId": %d,
                  "studentName": "赵六",
                  "studentPhone": "12345"
                }
                """.formatted(course.getId());

        mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-ENROLL-09: 报名邮箱格式错误应返回参数错误")
    void TC_ENROLL_09_invalidEmail() throws Exception {
        Course course = createCourse("邮箱格式课程", CourseStatus.ONLINE);
        String json = """
                {
                  "courseId": %d,
                  "studentName": "赵六",
                  "studentPhone": "13800138000",
                  "studentEmail": "bad-email"
                }
                """.formatted(course.getId());

        mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-ENROLL-10: 报名备注超长应返回参数错误")
    void TC_ENROLL_10_remarkTooLong() throws Exception {
        Course course = createCourse("备注超长课程", CourseStatus.ONLINE);
        String json = """
                {
                  "courseId": %d,
                  "studentName": "赵六",
                  "studentPhone": "13800138000",
                  "remark": "%s"
                }
                """.formatted(course.getId(), "a".repeat(501));

        mockMvc.perform(post("/api/public/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    private String validEnrollmentJson(Long courseId) {
        return """
                {
                  "courseId": %d,
                  "studentName": "李四",
                  "studentPhone": "13800138000",
                  "studentEmail": "lisi@example.com"
                }
                """.formatted(courseId);
    }
}
