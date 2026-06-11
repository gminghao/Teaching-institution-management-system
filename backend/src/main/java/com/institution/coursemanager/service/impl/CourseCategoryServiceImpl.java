package com.institution.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.institution.coursemanager.entity.CourseCategory;
import com.institution.coursemanager.mapper.CourseCategoryMapper;
import com.institution.coursemanager.service.CourseCategoryService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory>
        implements CourseCategoryService {

    private static final int ENABLED = 1;

    @Override
    public List<CourseCategory> listAll() {
        return list(new LambdaQueryWrapper<CourseCategory>()
                .eq(CourseCategory::getStatus, ENABLED)
                .orderByAsc(CourseCategory::getSortOrder)
                .orderByDesc(CourseCategory::getCreateTime));
    }
}
