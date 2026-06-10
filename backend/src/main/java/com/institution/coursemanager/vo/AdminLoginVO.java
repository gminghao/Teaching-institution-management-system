package com.institution.coursemanager.vo;

import lombok.Data;

@Data
public class AdminLoginVO {
    private String token;
    private String username;
    private String realName;
}
