package com.institution.coursemanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.mapper.EnrollmentOrderMapper;
import com.institution.coursemanager.service.FinanceService;
import com.institution.coursemanager.vo.FinanceSummaryVO;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FinanceServiceImpl extends ServiceImpl<EnrollmentOrderMapper, EnrollmentOrder>
        implements FinanceService {

    private final EnrollmentOrderMapper enrollmentOrderMapper;

    public FinanceServiceImpl(EnrollmentOrderMapper enrollmentOrderMapper) {
        this.enrollmentOrderMapper = enrollmentOrderMapper;
    }

    @Override
    public FinanceSummaryVO getFinanceSummary() {
        Map<String, Object> summary = enrollmentOrderMapper.selectFinanceSummary();
        FinanceSummaryVO vo = new FinanceSummaryVO();

        vo.setOrderCount(toLong(getSummaryValue(summary, "order_count")));
        vo.setTotalRegistrationFee(toBigDecimal(getSummaryValue(summary, "total_registration_fee")));
        vo.setTotalPaidAmount(toBigDecimal(getSummaryValue(summary, "total_paid_amount")));
        vo.setPaidCount(toLong(getSummaryValue(summary, "paid_count")));
        vo.setUnpaidCount(toLong(getSummaryValue(summary, "unpaid_count")));
        vo.setPartialCount(toLong(getSummaryValue(summary, "partial_count")));
        vo.setRefundedCount(toLong(getSummaryValue(summary, "refunded_count")));
        vo.setTotalUnpaidAmount(toBigDecimal(getSummaryValue(summary, "total_unpaid_amount")));
        vo.setTotalPartialAmount(toBigDecimal(getSummaryValue(summary, "total_partial_amount")));
        vo.setTotalRefundedAmount(toBigDecimal(getSummaryValue(summary, "total_refunded_amount")));

        return vo;
    }

    private Object getSummaryValue(Map<String, Object> summary, String key) {
        Object value = summary.get(key);
        if (value != null) {
            return value;
        }
        return summary.get(key.toUpperCase());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }
}
