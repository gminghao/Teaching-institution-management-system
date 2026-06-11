package com.institution.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.institution.coursemanager.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    @Select("SELECT id, username, password, real_name, status, create_time, update_time " +
            "FROM admin_user WHERE username = #{username} AND status = 1 LIMIT 1")
    AdminUser selectByUsername(@Param("username") String username);
}
