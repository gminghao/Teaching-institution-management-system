package com.institution.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.institution.coursemanager.dto.CourseCreateDTO;
import com.institution.coursemanager.dto.CourseUpdateDTO;
import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.vo.AdminCourseVO;
import com.institution.coursemanager.vo.CategoryVO;
import com.institution.coursemanager.vo.PageResult;
import com.institution.coursemanager.vo.PublicCourseDetailVO;
import com.institution.coursemanager.vo.PublicCourseVO;
import java.util.List;

public interface CourseService extends IService<Course> {

    AdminCourseVO createCourse(CourseCreateDTO dto);

    AdminCourseVO updateCourse(Long id, CourseUpdateDTO dto);

    void deleteCourse(Long id);

    void onlineCourse(Long id);

    void offlineCourse(Long id);

    AdminCourseVO getAdminCourseDetail(Long id);

    PublicCourseDetailVO getPublicCourseDetail(Long id);

    PageResult<AdminCourseVO> getAdminCoursePage(Integer pageNum, Integer pageSize, String keyword, String status, Long categoryId);

    PageResult<PublicCourseVO> getPublicCoursePage(Integer pageNum, Integer pageSize, Long categoryId, String keyword);

    List<CategoryVO> getCategories();
}
