package com.institution.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.institution.coursemanager.dto.AdminLoginDTO;
import com.institution.coursemanager.entity.AdminUser;
import com.institution.coursemanager.exception.UnauthorizedException;
import com.institution.coursemanager.mapper.AdminUserMapper;
import com.institution.coursemanager.service.AuthService;
import com.institution.coursemanager.util.JwtUtil;
import com.institution.coursemanager.vo.AdminLoginVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AuthService {

    private static final int ENABLED = 1;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // 登录失败次数记录: key=username, value=LoginAttempt
    private final ConcurrentHashMap<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    // Token黑名单: key=token, value=过期时间戳
    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    public AuthServiceImpl(JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 登录尝试记录内部类
     */
    private static class LoginAttempt {
        int attempts;
        long lockoutUntil;

        LoginAttempt(int attempts, long lockoutUntil) {
            this.attempts = attempts;
            this.lockoutUntil = lockoutUntil;
        }
    }

    @Override
    public AdminLoginVO login(AdminLoginDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new UnauthorizedException("用户名或密码错误");
        }

        String username = dto.getUsername();

        // 检查账号是否被锁定
        if (isAccountLocked(username)) {
            throw new UnauthorizedException("账号已锁定，请" + LOCKOUT_DURATION_MINUTES + "分钟后再试");
        }

        AdminUser adminUser = getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username)
                .eq(AdminUser::getStatus, ENABLED)
                .last("LIMIT 1"));

        if (adminUser == null || !passwordEncoder.matches(dto.getPassword(), adminUser.getPassword())) {
            // 登录失败，增加失败次数
            recordLoginFailure(username);
            throw new UnauthorizedException("用户名或密码错误");
        }

        // 登录成功，清除失败记录
        loginAttempts.remove(username);

        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(jwtUtil.generateToken(adminUser.getUsername()));
        vo.setUsername(adminUser.getUsername());
        vo.setRealName(adminUser.getRealName());
        return vo;
    }

    @Override
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 将Token加入黑名单，设置过期时间为24小时后
        long expiration = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24);
        tokenBlacklist.put(token, expiration);
        // 清理过期的黑名单Token
        cleanExpiredTokens();
    }

    /**
     * 检查账号是否被锁定
     */
    private boolean isAccountLocked(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt == null) {
            return false;
        }
        // 检查锁定是否已过期
        if (System.currentTimeMillis() > attempt.lockoutUntil) {
            loginAttempts.remove(username);
            return false;
        }
        return attempt.attempts >= MAX_LOGIN_ATTEMPTS;
    }

    /**
     * 记录登录失败
     */
    private void recordLoginFailure(String username) {
        loginAttempts.compute(username, (key, existing) -> {
            if (existing == null) {
                return new LoginAttempt(1, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(LOCKOUT_DURATION_MINUTES));
            }
            existing.attempts++;
            if (existing.attempts >= MAX_LOGIN_ATTEMPTS) {
                existing.lockoutUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(LOCKOUT_DURATION_MINUTES);
            }
            return existing;
        });
    }

    /**
     * 清理过期的Token黑名单
     */
    private void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        tokenBlacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    @Override
    public String validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("登录令牌不能为空");
        }
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 检查Token是否在黑名单中
        if (tokenBlacklist.containsKey(token)) {
            throw new UnauthorizedException("登录令牌已失效");
        }
        return jwtUtil.parseUsername(token);
    }
}
