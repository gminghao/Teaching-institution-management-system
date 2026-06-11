package com.institution.coursemanager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.enums.PaymentStatus;
import com.institution.coursemanager.mapper.EnrollmentOrderMapper;
import com.institution.coursemanager.service.FinanceService;
import com.institution.coursemanager.vo.FinanceSummaryVO;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FinanceServiceImpl extends ServiceImpl<EnrollmentOrderMapper, EnrollmentOrder>
        implements FinanceService {

    @Override
    public FinanceSummaryVO getFinanceSummary() {
        List<EnrollmentOrder> orders = list();
        FinanceSummaryVO vo = new FinanceSummaryVO();
        vo.setTotalRegistrationFee(BigDecimal.ZERO);
        vo.setTotalPaidAmount(BigDecimal.ZERO);
        vo.setTotalUnpaidAmount(BigDecimal.ZERO);
        vo.setTotalPartialAmount(BigDecimal.ZERO);
        vo.setTotalRefundedAmount(BigDecimal.ZERO);
        vo.setOrderCount((long) orders.size());
        vo.setPaidCount(0L);
        vo.setUnpaidCount(0L);
        vo.setPartialCount(0L);
        vo.setRefundedCount(0L);

        for (EnrollmentOrder order : orders) {
            BigDecimal registrationFee = safeAmount(order.getRegistrationFee());
            BigDecimal paidAmount = safeAmount(order.getPaidAmount());
            vo.setTotalRegistrationFee(vo.getTotalRegistrationFee().add(registrationFee));
            vo.setTotalPaidAmount(vo.getTotalPaidAmount().add(paidAmount));

            PaymentStatus status = PaymentStatus.fromCode(order.getPaymentStatus());
            if (status == PaymentStatus.PAID) {
                vo.setPaidCount(vo.getPaidCount() + 1);
            } else if (status == PaymentStatus.UNPAID) {
                vo.setUnpaidCount(vo.getUnpaidCount() + 1);
                vo.setTotalUnpaidAmount(vo.getTotalUnpaidAmount().add(registrationFee));
            } else if (status == PaymentStatus.PARTIAL) {
                vo.setPartialCount(vo.getPartialCount() + 1);
                vo.setTotalPartialAmount(vo.getTotalPartialAmount().add(paidAmount));
                vo.setTotalUnpaidAmount(vo.getTotalUnpaidAmount().add(remainingAmount(registrationFee, paidAmount)));
            } else if (status == PaymentStatus.REFUNDED) {
                vo.setRefundedCount(vo.getRefundedCount() + 1);
                vo.setTotalRefundedAmount(vo.getTotalRefundedAmount().add(paidAmount));
            }
        }
        return vo;
    }

    private BigDecimal remainingAmount(BigDecimal registrationFee, BigDecimal paidAmount) {
        BigDecimal remaining = registrationFee.subtract(paidAmount);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
