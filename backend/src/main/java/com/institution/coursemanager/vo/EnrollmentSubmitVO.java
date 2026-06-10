package com.institution.coursemanager.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class EnrollmentSubmitVO {
    private String orderNo;
    private String courseTitle;
    private BigDecimal registrationFee;
    private String message;
}
