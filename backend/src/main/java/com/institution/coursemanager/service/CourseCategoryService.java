package com.institution.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.institution.coursemanager.entity.CourseCategory;
import java.util.List;

public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategory> listAll();
}
