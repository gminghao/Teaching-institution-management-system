package com.institution.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.vo.AdminCourseVO;
import com.institution.coursemanager.vo.PublicCourseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    Page<PublicCourseVO> selectPublicPage(Page<PublicCourseVO> page,
                                          @Param("categoryId") Long categoryId,
                                          @Param("keyword") String keyword);

    Page<AdminCourseVO> selectAdminPage(Page<AdminCourseVO> page,
                                        @Param("categoryId") Long categoryId,
                                        @Param("keyword") String keyword,
                                        @Param("status") String status);
}
