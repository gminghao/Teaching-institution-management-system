package com.institution.coursemanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EnrollmentStatusDTO {

    @NotBlank(message = "报名状态不能为空")
    @Pattern(regexp = "^(PENDING|CONTACTED|ENROLLED|CANCELLED)$", message = "报名状态值无效")
    private String enrollmentStatus;
}
