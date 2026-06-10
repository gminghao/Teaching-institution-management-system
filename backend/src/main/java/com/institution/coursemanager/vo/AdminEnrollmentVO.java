package com.institution.coursemanager.vo;

import com.institution.coursemanager.enums.EnrollmentStatus;
import com.institution.coursemanager.enums.PaymentStatus;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class AdminEnrollmentVO {
    private Long id;
    private String orderNo;
    private Long courseId;
    private String courseTitle;
    private String studentName;
    private String studentPhone;
    private String studentEmail;
    private BigDecimal registrationFee;
    private BigDecimal paidAmount;
    private PaymentStatus paymentStatus;
    private EnrollmentStatus enrollmentStatus;
    private String createTime;
}
