package com.institution.coursemanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnrollmentSubmitDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 50, message = "姓名长度2-50字")
    private String studentName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String studentPhone;

    @Email(message = "邮箱格式不正确")
    private String studentEmail;

    @Size(max = 500, message = "备注最多500字")
    private String remark;
}
