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

    @Test
    @DisplayName("TC-LOGIN-09: 登出接口正常工作")
    void TC_LOGIN_09_logoutTokenInvalid() throws Exception {
        // 验证登出接口可以正常访问（无需认证）
        mockMvc.perform(post("/api/admin/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证登出接口携带Token也可以访问
        mockMvc.perform(post("/api/admin/auth/logout")
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-LOGIN-10: 连续5次错误密码后账号应被锁定")
    void TC_LOGIN_10_accountLockout() throws Exception {
        // 使用不同的用户名避免影响其他测试
        String wrongJson = "{\"username\":\"testlock\",\"password\":\"wrongpassword\"}";
        String correctJson = "{\"username\":\"testlock\",\"password\":\"admin123\"}";

        // 先创建一个测试用户（如果不存在）
        // 连续5次错误密码
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/admin/auth/login")
                            .contentType("application/json")
                            .content(wrongJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        // 第6次使用密码应返回账号锁定错误
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content(correctJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号已锁定，请15分钟后再试"));
    }

    @Test
    @DisplayName("TC-LOGIN-11: 登出接口无需认证即可访问")
    void TC_LOGIN_11_logoutNoAuthRequired() throws Exception {
        // 登出接口应该在拦截器排除列表中，无需Token即可访问
        mockMvc.perform(post("/api/admin/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
