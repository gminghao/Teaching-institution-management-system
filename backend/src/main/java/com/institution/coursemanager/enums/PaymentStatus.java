package com.institution.coursemanager.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    UNPAID("UNPAID", "未缴费"),
    PARTIAL("PARTIAL", "部分缴费"),
    PAID("PAID", "已缴费"),
    REFUNDED("REFUNDED", "已退款");

    private final String code;
    private final String desc;

    PaymentStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status code: " + code);
    }
}
