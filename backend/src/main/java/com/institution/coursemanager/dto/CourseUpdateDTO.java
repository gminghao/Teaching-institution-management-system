package com.institution.coursemanager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CourseUpdateDTO {

    private Long categoryId;

    @Size(min = 1, max = 200, message = "标题长度1-200字")
    private String title;

    private String subtitle;

    private String coverImage;

    private String description;

    private String teacherName;

    @DecimalMin(value = "0", message = "价格不能为负")
    private BigDecimal price;

    @DecimalMin(value = "0", message = "报名费不能为负")
    private BigDecimal registrationFee;
}
