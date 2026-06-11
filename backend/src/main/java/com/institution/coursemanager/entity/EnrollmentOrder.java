package com.institution.coursemanager.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("enrollment_order")
public class EnrollmentOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long courseId;

    private String courseTitle;

    private String studentName;

    private String studentPhone;

    private String studentEmail;

    private String remark;

    private BigDecimal registrationFee;

    private BigDecimal paidAmount;

    private String paymentStatus;

    private String enrollmentStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
