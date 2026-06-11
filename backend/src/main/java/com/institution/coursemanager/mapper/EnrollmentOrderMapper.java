package com.institution.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.institution.coursemanager.entity.EnrollmentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface EnrollmentOrderMapper extends BaseMapper<EnrollmentOrder> {

    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM enrollment_order WHERE id = #{orderId}")
    BigDecimal sumPaidAmount(@Param("orderId") Long orderId);
}
