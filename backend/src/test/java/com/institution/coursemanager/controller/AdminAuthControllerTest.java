package com.institution.coursemanager.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AdminAuthController - 管理员认证接口测试")
class AdminAuthControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("TC-LOGIN-01: 正常登录应返回 token")
    void TC_LOGIN_01_normalLogin() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("TC-LOGIN-02: 用户名为空应返回错误")
    void TC_LOGIN_02_emptyUsername() throws Exception {
        String json = "{\"username\":\"\",\"password\":\"admin123\"}";
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-LOGIN-03: 密码为空应返回错误")
    void TC_LOGIN_03_emptyPassword() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"\"}";
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-LOGIN-04: 用户名和密码都为空应返回错误")
    void TC_LOGIN_04_bothEmpty() throws Exception {
        String json = "{\"username\":\"\",\"password\":\"\"}";
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-LOGIN-05: 错误密码应返回未授权")
    void TC_LOGIN_05_wrongPassword() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"wrongpassword\"}";
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("TC-LOGIN-06: 不存在的用户名应返回未授权")
    void TC_LOGIN_06_nonExistentUser() throws Exception {
        String json = "{\"username\":\"nonexistent\",\"password\":\"admin123\"}";
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("TC-LOGIN-07: 请求体为空应返回错误")
    void TC_LOGIN_07_emptyBody() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-LOGIN-08: 无 Content-Type 应返回 415")
    void TC_LOGIN_08_noContentType() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        mockMvc.perform(post("/api/admin/auth/login")
                        .content(json))
                .andExpect(status().isUnsupportedMediaType());
    }
}
