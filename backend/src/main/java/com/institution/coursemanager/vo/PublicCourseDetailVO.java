package com.institution.coursemanager.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PublicCourseDetailVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String subtitle;
    private String coverImage;
    private String teacherName;
    private BigDecimal price;
    private BigDecimal registrationFee;
    private String description;
}
