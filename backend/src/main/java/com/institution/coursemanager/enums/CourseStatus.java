package com.institution.coursemanager.enums;

import lombok.Getter;

@Getter
public enum CourseStatus {
    DRAFT("DRAFT", "草稿"),
    ONLINE("ONLINE", "已上架"),
    OFFLINE("OFFLINE", "已下架");

    private final String code;
    private final String desc;

    CourseStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CourseStatus fromCode(String code) {
        for (CourseStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown course status code: " + code);
    }
}
