package com.institution.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.institution.coursemanager.dto.PaymentCreateDTO;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.entity.PaymentRecord;
import com.institution.coursemanager.enums.PaymentMethod;
import com.institution.coursemanager.enums.PaymentStatus;
import com.institution.coursemanager.exception.ConflictException;
import com.institution.coursemanager.exception.NotFoundException;
import com.institution.coursemanager.exception.ValidationException;
import com.institution.coursemanager.mapper.EnrollmentOrderMapper;
import com.institution.coursemanager.mapper.PaymentRecordMapper;
import com.institution.coursemanager.service.PaymentService;
import com.institution.coursemanager.vo.PaymentRecordVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EnrollmentOrderMapper enrollmentOrderMapper;

    public PaymentServiceImpl(EnrollmentOrderMapper enrollmentOrderMapper) {
        this.enrollmentOrderMapper = enrollmentOrderMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecordVO createPayment(PaymentCreateDTO dto) {
        validatePaymentDTO(dto);
        EnrollmentOrder order = enrollmentOrderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new NotFoundException("报名订单不存在");
        }
        if (PaymentStatus.REFUNDED.getCode().equals(order.getPaymentStatus())) {
            throw new ConflictException("已退款订单不能继续确认缴费");
        }

        BigDecimal paidAmount = safeAmount(order.getPaidAmount());
        BigDecimal registrationFee = safeAmount(order.getRegistrationFee());
        BigDecimal newPaidAmount = paidAmount.add(dto.getAmount());
        if (newPaidAmount.compareTo(registrationFee) > 0) {
            throw new ConflictException("缴费金额不能超过报名费");
        }

        LocalDateTime now = LocalDateTime.now();
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(dto.getOrderId());
        record.setAmount(dto.getAmount());
        record.setPaymentMethod(parsePaymentMethod(dto.getPaymentMethod()).getCode());
        record.setPaymentTime(parsePaymentTime(dto.getPaymentTime(), now));
        record.setOperatorName(dto.getOperatorName());
        record.setRemark(dto.getRemark());
        record.setCreateTime(now);
        save(record);

        order.setPaidAmount(newPaidAmount);
        order.setPaymentStatus(resolvePaymentStatus(newPaidAmount, registrationFee).getCode());
        order.setUpdateTime(now);
        enrollmentOrderMapper.updateById(order);

        return toPaymentRecordVO(record);
    }

    @Override
    public List<PaymentRecordVO> getPaymentRecords(Long orderId) {
        if (orderId == null) {
            throw new ValidationException("报名订单ID不能为空");
        }
        return list(new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
                        .orderByDesc(PaymentRecord::getPaymentTime)
                        .orderByDesc(PaymentRecord::getCreateTime))
                .stream()
                .map(this::toPaymentRecordVO)
                .toList();
    }

    private void validatePaymentDTO(PaymentCreateDTO dto) {
        if (dto == null) {
            throw new ValidationException("缴费内容不能为空");
        }
        if (dto.getOrderId() == null) {
            throw new ValidationException("订单ID不能为空");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("缴费金额必须大于0");
        }
        if (!StringUtils.hasText(dto.getPaymentMethod())) {
            throw new ValidationException("支付方式不能为空");
        }
    }

    private PaymentMethod parsePaymentMethod(String paymentMethod) {
        try {
            return PaymentMethod.fromCode(paymentMethod);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("支付方式值无效");
        }
    }

    private PaymentStatus resolvePaymentStatus(BigDecimal paidAmount, BigDecimal registrationFee) {
        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentStatus.UNPAID;
        }
        if (paidAmount.compareTo(registrationFee) >= 0) {
            return PaymentStatus.PAID;
        }
        return PaymentStatus.PARTIAL;
    }

    private LocalDateTime parsePaymentTime(String paymentTime, LocalDateTime defaultTime) {
        if (!StringUtils.hasText(paymentTime)) {
            return defaultTime;
        }
        try {
            return LocalDateTime.parse(paymentTime, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(paymentTime);
            } catch (DateTimeParseException ex) {
                throw new ValidationException("缴费时间格式无效");
            }
        }
    }

    private PaymentRecordVO toPaymentRecordVO(PaymentRecord record) {
        PaymentRecordVO vo = new PaymentRecordVO();
        vo.setId(record.getId());
        vo.setOrderId(record.getOrderId());
        vo.setAmount(record.getAmount());
        vo.setPaymentMethod(parsePaymentMethod(record.getPaymentMethod()));
        vo.setPaymentTime(record.getPaymentTime() == null ? null : record.getPaymentTime().format(DATE_TIME_FORMATTER));
        vo.setOperatorName(record.getOperatorName());
        vo.setRemark(record.getRemark());
        return vo;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
