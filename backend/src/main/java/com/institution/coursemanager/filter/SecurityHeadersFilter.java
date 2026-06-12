package com.institution.coursemanager.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 安全响应头过滤器
 * 添加 X-Frame-Options、X-Content-Type-Options 等安全头
 */
@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 防止点击劫持
        httpResponse.setHeader("X-Frame-Options", "DENY");
        // 防止MIME类型嗅探
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        // 启用XSS过滤
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        chain.doFilter(request, response);
    }
}