package com.institution.coursemanager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentCreateDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "缴费金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "支付方式不能为空")
    @Pattern(regexp = "^(CASH|WECHAT|ALIPAY|BANK)$", message = "支付方式值无效")
    private String paymentMethod;

    private String paymentTime;

    private String operatorName;

    @Size(max = 500, message = "备注最多500字")
    private String remark;
}
