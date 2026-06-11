package com.institution.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.institution.coursemanager.dto.AdminLoginDTO;
import com.institution.coursemanager.entity.AdminUser;
import com.institution.coursemanager.vo.AdminLoginVO;

public interface AuthService extends IService<AdminUser> {

    AdminLoginVO login(AdminLoginDTO dto);

    String validateToken(String token);
}
