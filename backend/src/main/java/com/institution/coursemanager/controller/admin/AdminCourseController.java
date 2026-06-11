package com.institution.coursemanager.controller.admin;

import com.institution.coursemanager.dto.CourseCreateDTO;
import com.institution.coursemanager.dto.CourseUpdateDTO;
import com.institution.coursemanager.service.CourseService;
import com.institution.coursemanager.vo.AdminCourseVO;
import com.institution.coursemanager.vo.PageResult;
import com.institution.coursemanager.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端 - 课程管理
 */
@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 课程列表（支持关键字和状态筛选）
     */
    @GetMapping
    public Result<PageResult<AdminCourseVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(courseService.getAdminCoursePage(pageNum, pageSize, keyword, status));
    }

    /**
     * 创建课程
     */
    @PostMapping
    public Result<AdminCourseVO> create(@Valid @RequestBody CourseCreateDTO dto) {
        return Result.success(courseService.createCourse(dto));
    }

    /**
     * 更新课程
     */
    @PutMapping("/{id}")
    public Result<AdminCourseVO> update(@PathVariable Long id, @Valid @RequestBody CourseUpdateDTO dto) {
        return Result.success(courseService.updateCourse(id, dto));
    }

    /**
     * 上线课程
     */
    @PutMapping("/{id}/online")
    public Result<Void> online(@PathVariable Long id) {
        courseService.onlineCourse(id);
        return Result.success();
    }

    /**
     * 下线课程
     */
    @PutMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        courseService.offlineCourse(id);
        return Result.success();
    }
}
