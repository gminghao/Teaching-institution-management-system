package com.institution.coursemanager.controller.admin;

import com.institution.coursemanager.dto.PaymentCreateDTO;
import com.institution.coursemanager.service.PaymentService;
import com.institution.coursemanager.vo.PaymentRecordVO;
import com.institution.coursemanager.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端 - 缴费管理
 */
@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建缴费记录
     */
    @PostMapping
    public Result<PaymentRecordVO> create(@Valid @RequestBody PaymentCreateDTO dto) {
        return Result.success(paymentService.createPayment(dto));
    }
}
