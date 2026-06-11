package com.institution.coursemanager.controller;

import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.enums.CourseStatus;
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
@DisplayName("PublicCourseController - 访客课程浏览接口测试")
class PublicCourseControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("TC-COURSE-15: 访客查询课程列表应只返回已上架课程")
    void TC_COURSE_15_listOnlineCourses() throws Exception {
        createCourse("访客可见课程", CourseStatus.ONLINE);
        createCourse("访客不可见课程", CourseStatus.OFFLINE);

        mockMvc.perform(get("/api/public/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list[0].title").value("访客可见课程"));
    }

    @Test
    @DisplayName("TC-COURSE-16: 访客按分类筛选课程应返回匹配课程")
    void TC_COURSE_16_filterCoursesByCategory() throws Exception {
        Course course = createCourse("分类筛选课程", CourseStatus.ONLINE);

        mockMvc.perform(get("/api/public/courses")
                        .param("categoryId", course.getCategoryId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].categoryId").value(course.getCategoryId()))
                .andExpect(jsonPath("$.data.list[0].title").value("分类筛选课程"));
    }

    @Test
    @DisplayName("TC-COURSE-17: 访客按关键字搜索课程应返回匹配课程")
    void TC_COURSE_17_searchCoursesByKeyword() throws Exception {
        createCourse("少儿 Python 编程", CourseStatus.ONLINE);

        mockMvc.perform(get("/api/public/courses")
                        .param("keyword", "Python")
                        .param("pageNum", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.list[0].title").value("少儿 Python 编程"));
    }

    @Test
    @DisplayName("TC-COURSE-18: 访客查看已上架课程详情应返回课程详情")
    void TC_COURSE_18_getOnlineCourseDetail() throws Exception {
        Course course = createCourse("课程详情测试", CourseStatus.ONLINE);

        mockMvc.perform(get("/api/public/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(course.getId()))
                .andExpect(jsonPath("$.data.title").value("课程详情测试"))
                .andExpect(jsonPath("$.data.description").exists())
                .andExpect(jsonPath("$.data.registrationFee").exists());
    }

    @Test
    @DisplayName("TC-COURSE-19: 访客查看未上架课程详情应返回未找到")
    void TC_COURSE_19_getOfflineCourseDetail() throws Exception {
        Course course = createCourse("未上架课程详情", CourseStatus.DRAFT);

        mockMvc.perform(get("/api/public/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
