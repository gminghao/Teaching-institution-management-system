package com.institution.coursemanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.institution.coursemanager.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

    @Select("SELECT id, order_id, amount, payment_method, payment_time, operator_name, remark, create_time " +
            "FROM payment_record WHERE order_id = #{orderId} ORDER BY payment_time DESC, create_time DESC")
    List<PaymentRecord> selectByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM payment_record WHERE order_id = #{orderId}")
    BigDecimal sumByOrderId(@Param("orderId") Long orderId);
}
