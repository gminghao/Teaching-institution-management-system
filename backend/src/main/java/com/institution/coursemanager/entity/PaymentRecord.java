package com.institution.coursemanager.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("payment_record")
public class PaymentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private BigDecimal amount;

    private String paymentMethod;

    private LocalDateTime paymentTime;

    private String operatorName;

    private String remark;

    private LocalDateTime createTime;
}
