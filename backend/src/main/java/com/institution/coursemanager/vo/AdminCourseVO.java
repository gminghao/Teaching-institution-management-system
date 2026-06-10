package com.institution.coursemanager.vo;

import com.institution.coursemanager.enums.CourseStatus;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class AdminCourseVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String subtitle;
    private String coverImage;
    private String description;
    private String teacherName;
    private BigDecimal price;
    private BigDecimal registrationFee;
    private CourseStatus status;
    private String createTime;
    private String updateTime;
}
