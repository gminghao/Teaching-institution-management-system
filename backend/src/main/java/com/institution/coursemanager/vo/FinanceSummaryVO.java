package com.institution.coursemanager.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FinanceSummaryVO {
    private BigDecimal totalRegistrationFee;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalUnpaidAmount;
    private BigDecimal totalPartialAmount;
    private BigDecimal totalRefundedAmount;
    private Long orderCount;
    private Long paidCount;
    private Long unpaidCount;
    private Long partialCount;
    private Long refundedCount;
}
