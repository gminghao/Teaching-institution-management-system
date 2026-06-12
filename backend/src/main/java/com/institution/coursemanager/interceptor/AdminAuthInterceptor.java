package com.institution.coursemanager.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.institution.coursemanager.service.AuthService;
import com.institution.coursemanager.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理员端 JWT 认证拦截器
 */
@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 使用AuthService验证Token（包含黑名单检查）
                String username = authService.validateToken(token);
                request.setAttribute("currentUsername", username);
                return true;
            } catch (Exception e) {
                log.warn("Token validation failed: {}", e.getMessage());
                // Token 无效，继续返回 401
            }
        }

        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.unauthorized("未登录或登录已过期")));
        return false;
    }
}
