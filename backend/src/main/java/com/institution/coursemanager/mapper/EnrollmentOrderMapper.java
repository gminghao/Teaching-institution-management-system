package com.institution.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.institution.coursemanager.entity.EnrollmentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface EnrollmentOrderMapper extends BaseMapper<EnrollmentOrder> {

    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM enrollment_order WHERE id = #{orderId}")
    BigDecimal sumPaidAmount(@Param("orderId") Long orderId);

    /**
     * 财务汇总聚合查询
     */
    @Select("""
        SELECT
            COUNT(*) as order_count,
            COALESCE(SUM(registration_fee), 0) as total_registration_fee,
            COALESCE(SUM(paid_amount), 0) as total_paid_amount,
            SUM(CASE WHEN payment_status = 'PAID' THEN 1 ELSE 0 END) as paid_count,
            SUM(CASE WHEN payment_status = 'UNPAID' THEN 1 ELSE 0 END) as unpaid_count,
            SUM(CASE WHEN payment_status = 'PARTIAL' THEN 1 ELSE 0 END) as partial_count,
            SUM(CASE WHEN payment_status = 'REFUNDED' THEN 1 ELSE 0 END) as refunded_count,
            COALESCE(SUM(CASE WHEN payment_status = 'UNPAID' THEN registration_fee ELSE 0 END), 0) as total_unpaid_amount,
            COALESCE(SUM(CASE WHEN payment_status = 'PARTIAL' THEN paid_amount ELSE 0 END), 0) as total_partial_amount,
            COALESCE(SUM(CASE WHEN payment_status = 'REFUNDED' THEN paid_amount ELSE 0 END), 0) as total_refunded_amount
        FROM enrollment_order
        """)
    Map<String, Object> selectFinanceSummary();
}
