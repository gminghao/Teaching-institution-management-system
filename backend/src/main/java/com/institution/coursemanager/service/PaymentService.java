package com.institution.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.institution.coursemanager.dto.PaymentCreateDTO;
import com.institution.coursemanager.entity.PaymentRecord;
import com.institution.coursemanager.vo.PaymentRecordVO;
import java.util.List;

public interface PaymentService extends IService<PaymentRecord> {

    PaymentRecordVO createPayment(PaymentCreateDTO dto);

    List<PaymentRecordVO> getPaymentRecords(Long orderId);
}
