package com.institution.coursemanager.controller.visitor;

import com.institution.coursemanager.service.CourseService;
import com.institution.coursemanager.vo.PageResult;
import com.institution.coursemanager.vo.PublicCourseDetailVO;
import com.institution.coursemanager.vo.PublicCourseVO;
import com.institution.coursemanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访客端 - 课程浏览
 */
@RestController
@RequestMapping("/api/public/courses")
public class PublicCourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 课程列表（支持分类筛选和关键字搜索）
     */
    @GetMapping
    public Result<PageResult<PublicCourseVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(courseService.getPublicCoursePage(pageNum, pageSize, categoryId, keyword));
    }

    /**
     * 课程详情
     */
    @GetMapping("/{id}")
    public Result<PublicCourseDetailVO> detail(@PathVariable Long id) {
        return Result.success(courseService.getPublicCourseDetail(id));
    }
}
