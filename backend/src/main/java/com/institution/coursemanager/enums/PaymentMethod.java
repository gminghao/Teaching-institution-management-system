package com.institution.coursemanager.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("CASH", "现金"),
    WECHAT("WECHAT", "微信"),
    ALIPAY("ALIPAY", "支付宝"),
    BANK("BANK", "银行转账");

    private final String code;
    private final String desc;

    PaymentMethod(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method code: " + code);
    }
}
