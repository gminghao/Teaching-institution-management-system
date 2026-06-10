package com.institution.coursemanager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CourseCreateDTO {

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "课程标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度1-200字")
    private String title;

    private String subtitle;

    private String coverImage;

    private String description;

    private String teacherName;

    @NotNull(message = "课程价格不能为空")
    @DecimalMin(value = "0", message = "价格不能为负")
    private BigDecimal price;

    @NotNull(message = "报名费不能为空")
    @DecimalMin(value = "0", message = "报名费不能为负")
    private BigDecimal registrationFee;
}
