package com.institution.coursemanager.vo;

import com.institution.coursemanager.enums.PaymentMethod;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentRecordVO {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String paymentTime;
    private String operatorName;
    private String remark;
}
