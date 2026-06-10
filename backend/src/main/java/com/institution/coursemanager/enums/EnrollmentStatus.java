package com.institution.coursemanager.enums;

import lombok.Getter;

@Getter
public enum EnrollmentStatus {
    PENDING("PENDING", "待处理"),
    CONTACTED("CONTACTED", "已联系"),
    ENROLLED("ENROLLED", "已报名"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    EnrollmentStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static EnrollmentStatus fromCode(String code) {
        for (EnrollmentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown enrollment status code: " + code);
    }
}
