package com.institution.coursemanager.controller;

import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.enums.CourseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AdminCourseController - 管理员课程接口测试")
class AdminCourseControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("TC-COURSE-01: 管理员查询课程列表应返回分页结构")
    void TC_COURSE_01_listCourses() throws Exception {
        createCourse("Java 基础课程", CourseStatus.ONLINE);

        mockMvc.perform(get("/api/admin/courses")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("TC-COURSE-02: 管理员按状态筛选课程应返回匹配数据")
    void TC_COURSE_02_filterCoursesByStatus() throws Exception {
        createCourse("线上课程", CourseStatus.ONLINE);
        createCourse("草稿课程", CourseStatus.DRAFT);

        mockMvc.perform(get("/api/admin/courses")
                        .param("status", "ONLINE")
                        .param("pageNum", "1")
                        .param("pageSize", "5")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.list[0].status").value("ONLINE"));
    }

    @Test
    @DisplayName("TC-COURSE-03: 管理员按关键字搜索课程应返回匹配数据")
    void TC_COURSE_03_searchCoursesByKeyword() throws Exception {
        createCourse("Python 数据分析", CourseStatus.ONLINE);

        mockMvc.perform(get("/api/admin/courses")
                        .param("keyword", "Python")
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].title").value("Python 数据分析"));
    }

    @Test
    @DisplayName("TC-COURSE-04: 管理员创建课程应返回草稿课程")
    void TC_COURSE_04_createCourse() throws Exception {
        String json = """
                {
                  "categoryId": 1,
                  "title": "Spring Boot 实战",
                  "subtitle": "后端开发",
                  "description": "Spring Boot 3 项目课程",
                  "teacherName": "Teacher Li",
                  "price": 299.00,
                  "registrationFee": 59.00
                }
                """;

        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value("Spring Boot 实战"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    @DisplayName("TC-COURSE-05: 创建课程缺少分类ID应返回参数错误")
    void TC_COURSE_05_createCourseMissingCategory() throws Exception {
        String json = """
                {
                  "title": "缺少分类课程",
                  "price": 199.00,
                  "registrationFee": 49.00
                }
                """;

        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-COURSE-06: 创建课程标题为空应返回参数错误")
    void TC_COURSE_06_createCourseEmptyTitle() throws Exception {
        String json = """
                {
                  "categoryId": 1,
                  "title": "",
                  "price": 199.00,
                  "registrationFee": 49.00
                }
                """;

        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-COURSE-07: 创建课程价格为负应返回参数错误")
    void TC_COURSE_07_createCourseNegativePrice() throws Exception {
        String json = """
                {
                  "categoryId": 1,
                  "title": "负价格课程",
                  "price": -1.00,
                  "registrationFee": 49.00
                }
                """;

        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-COURSE-08: 创建课程分类不存在应返回未找到")
    void TC_COURSE_08_createCourseCategoryNotFound() throws Exception {
        String json = """
                {
                  "categoryId": 9999,
                  "title": "不存在分类课程",
                  "price": 199.00,
                  "registrationFee": 49.00
                }
                """;

        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("TC-COURSE-09: 管理员更新课程应返回更新后的课程")
    void TC_COURSE_09_updateCourse() throws Exception {
        Course course = createCourse("待更新课程", CourseStatus.DRAFT);
        String json = """
                {
                  "title": "已更新课程",
                  "teacherName": "Teacher Zhao",
                  "price": 399.00,
                  "registrationFee": 79.00
                }
                """;

        mockMvc.perform(put("/api/admin/courses/{id}", course.getId())
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(course.getId()))
                .andExpect(jsonPath("$.data.title").value("已更新课程"))
                .andExpect(jsonPath("$.data.teacherName").value("Teacher Zhao"));
    }

    @Test
    @DisplayName("TC-COURSE-10: 更新不存在课程应返回未找到")
    void TC_COURSE_10_updateCourseNotFound() throws Exception {
        String json = "{\"title\":\"不存在课程\"}";

        mockMvc.perform(put("/api/admin/courses/{id}", 9999L)
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("TC-COURSE-11: 更新课程标题为空白应返回参数错误")
    void TC_COURSE_11_updateCourseBlankTitle() throws Exception {
        Course course = createCourse("空白标题课程", CourseStatus.DRAFT);
        String json = "{\"title\":\" \"}";

        mockMvc.perform(put("/api/admin/courses/{id}", course.getId())
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-COURSE-12: 更新课程报名费为负应返回参数错误")
    void TC_COURSE_12_updateCourseNegativeRegistrationFee() throws Exception {
        Course course = createCourse("负报名费课程", CourseStatus.DRAFT);
        String json = "{\"registrationFee\":-1.00}";

        mockMvc.perform(put("/api/admin/courses/{id}", course.getId())
                        .header("Authorization", bearerToken(getAdminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-COURSE-13: 管理员上架课程应返回成功")
    void TC_COURSE_13_onlineCourse() throws Exception {
        Course course = createCourse("待上架课程", CourseStatus.DRAFT);

        mockMvc.perform(put("/api/admin/courses/{id}/online", course.getId())
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("TC-COURSE-14: 管理员下架课程应返回成功")
    void TC_COURSE_14_offlineCourse() throws Exception {
        Course course = createCourse("待下架课程", CourseStatus.ONLINE);

        mockMvc.perform(put("/api/admin/courses/{id}/offline", course.getId())
                        .header("Authorization", bearerToken(getAdminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
