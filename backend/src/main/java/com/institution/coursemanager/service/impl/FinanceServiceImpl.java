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

        vo.setOrderCount(toLong(summary.get("order_count")));
        vo.setTotalRegistrationFee(toBigDecimal(summary.get("total_registration_fee")));
        vo.setTotalPaidAmount(toBigDecimal(summary.get("total_paid_amount")));
        vo.setPaidCount(toLong(summary.get("paid_count")));
        vo.setUnpaidCount(toLong(summary.get("unpaid_count")));
        vo.setPartialCount(toLong(summary.get("partial_count")));
        vo.setRefundedCount(toLong(summary.get("refunded_count")));
        vo.setTotalUnpaidAmount(toBigDecimal(summary.get("total_unpaid_amount")));
        vo.setTotalPartialAmount(toBigDecimal(summary.get("total_partial_amount")));
        vo.setTotalRefundedAmount(toBigDecimal(summary.get("total_refunded_amount")));

        return vo;
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
