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

@Service
public class AuthServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AuthService {

    private static final int ENABLED = 1;

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AdminLoginVO login(AdminLoginDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new UnauthorizedException("用户名或密码错误");
        }

        AdminUser adminUser = getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, dto.getUsername())
                .eq(AdminUser::getStatus, ENABLED)
                .last("LIMIT 1"));
        if (adminUser == null || !passwordEncoder.matches(dto.getPassword(), adminUser.getPassword())) {
            throw new UnauthorizedException("用户名或密码错误");
        }

        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(jwtUtil.generateToken(adminUser.getUsername()));
        vo.setUsername(adminUser.getUsername());
        vo.setRealName(adminUser.getRealName());
        return vo;
    }

    @Override
    public String validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("登录令牌不能为空");
        }
        return jwtUtil.parseUsername(token);
    }
}
