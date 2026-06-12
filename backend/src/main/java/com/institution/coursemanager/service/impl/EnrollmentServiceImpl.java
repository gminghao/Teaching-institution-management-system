package com.institution.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.institution.coursemanager.dto.EnrollmentStatusDTO;
import com.institution.coursemanager.dto.EnrollmentSubmitDTO;
import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.enums.CourseStatus;
import com.institution.coursemanager.enums.EnrollmentStatus;
import com.institution.coursemanager.enums.PaymentStatus;
import com.institution.coursemanager.exception.ConflictException;
import com.institution.coursemanager.exception.NotFoundException;
import com.institution.coursemanager.exception.ValidationException;
import com.institution.coursemanager.mapper.CourseMapper;
import com.institution.coursemanager.mapper.EnrollmentOrderMapper;
import com.institution.coursemanager.service.EnrollmentService;
import com.institution.coursemanager.vo.AdminEnrollmentVO;
import com.institution.coursemanager.vo.EnrollmentSubmitVO;
import com.institution.coursemanager.vo.PageResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EnrollmentServiceImpl extends ServiceImpl<EnrollmentOrderMapper, EnrollmentOrder>
        implements EnrollmentService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final CourseMapper courseMapper;
    private final EnrollmentOrderMapper enrollmentOrderMapper;

    public EnrollmentServiceImpl(CourseMapper courseMapper, EnrollmentOrderMapper enrollmentOrderMapper) {
        this.courseMapper = courseMapper;
        this.enrollmentOrderMapper = enrollmentOrderMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EnrollmentSubmitVO submitEnrollment(EnrollmentSubmitDTO dto) {
        validateSubmitDTO(dto);
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null || !CourseStatus.ONLINE.getCode().equals(course.getStatus())) {
            throw new NotFoundException("课程不存在或未上架");
        }

        LocalDateTime now = LocalDateTime.now();
        EnrollmentOrder order = new EnrollmentOrder();
        order.setOrderNo(generateOrderNo(now));
        order.setCourseId(course.getId());
        order.setCourseTitle(course.getTitle());
        order.setStudentName(dto.getStudentName());
        order.setStudentPhone(dto.getStudentPhone());
        order.setStudentEmail(dto.getStudentEmail());
        order.setRemark(dto.getRemark());
        order.setRegistrationFee(course.getRegistrationFee());
        order.setPaidAmount(BigDecimal.ZERO);
        order.setPaymentStatus(PaymentStatus.UNPAID.getCode());
        order.setEnrollmentStatus(EnrollmentStatus.PENDING.getCode());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        save(order);

        EnrollmentSubmitVO vo = new EnrollmentSubmitVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setCourseTitle(order.getCourseTitle());
        vo.setRegistrationFee(order.getRegistrationFee());
        vo.setMessage("报名提交成功，请等待管理员联系确认");
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEnrollmentStatus(Long orderId, EnrollmentStatusDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getEnrollmentStatus())) {
            throw new ValidationException("报名状态不能为空");
        }
        EnrollmentStatus targetStatus = parseEnrollmentStatus(dto.getEnrollmentStatus());
        EnrollmentOrder order = requireOrder(orderId);
        EnrollmentStatus currentStatus = parseEnrollmentStatus(order.getEnrollmentStatus());
        validateStatusTransition(currentStatus, targetStatus);
        order.setEnrollmentStatus(targetStatus.getCode());
        order.setUpdateTime(LocalDateTime.now());
        enrollmentOrderMapper.updateById(order);
    }

    @Override
    public PageResult<AdminEnrollmentVO> getEnrollmentList(
            Integer pageNum,
            Integer pageSize,
            String keyword,
            String paymentStatus,
            String enrollmentStatus) {
        LambdaQueryWrapper<EnrollmentOrder> wrapper = new LambdaQueryWrapper<EnrollmentOrder>()
                .and(StringUtils.hasText(keyword), query -> query
                        .like(EnrollmentOrder::getOrderNo, keyword)
                        .or()
                        .like(EnrollmentOrder::getStudentName, keyword)
                        .or()
                        .like(EnrollmentOrder::getStudentPhone, keyword)
                        .or()
                        .like(EnrollmentOrder::getCourseTitle, keyword))
                .eq(StringUtils.hasText(paymentStatus), EnrollmentOrder::getPaymentStatus, paymentStatus)
                .eq(StringUtils.hasText(enrollmentStatus), EnrollmentOrder::getEnrollmentStatus, enrollmentStatus)
                .orderByDesc(EnrollmentOrder::getCreateTime);
        Page<EnrollmentOrder> page = page(new Page<>(normalizePageNum(pageNum), normalizePageSize(pageSize)), wrapper);
        List<AdminEnrollmentVO> records = page.getRecords().stream()
                .map(this::toAdminEnrollmentVO)
                .toList();
        return PageResult.of(page.getTotal(), (int) page.getCurrent(), (int) page.getSize(), records);
    }

    private void validateSubmitDTO(EnrollmentSubmitDTO dto) {
        if (dto == null) {
            throw new ValidationException("报名内容不能为空");
        }
        if (dto.getCourseId() == null) {
            throw new ValidationException("课程ID不能为空");
        }
        if (!StringUtils.hasText(dto.getStudentName())) {
            throw new ValidationException("姓名不能为空");
        }
        if (!StringUtils.hasText(dto.getStudentPhone())) {
            throw new ValidationException("手机号不能为空");
        }
    }

    private EnrollmentOrder requireOrder(Long orderId) {
        if (orderId == null) {
            throw new ValidationException("报名订单ID不能为空");
        }
        EnrollmentOrder order = enrollmentOrderMapper.selectById(orderId);
        if (order == null) {
            throw new NotFoundException("报名订单不存在");
        }
        return order;
    }

    private void validateStatusTransition(EnrollmentStatus currentStatus, EnrollmentStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return;
        }
        // 终态不可变更
        if (currentStatus == EnrollmentStatus.CANCELLED) {
            throw new ConflictException("已取消的报名不能继续变更状态");
        }
        if (currentStatus == EnrollmentStatus.ENROLLED) {
            throw new ConflictException("已录取的报名不能继续变更状态");
        }
        // 状态转换规则：只允许特定转换
        switch (currentStatus) {
            case PENDING:
                if (targetStatus != EnrollmentStatus.CONTACTED && targetStatus != EnrollmentStatus.CANCELLED) {
                    throw new ConflictException("待处理状态只能变更为已联系或已取消");
                }
                break;
            case CONTACTED:
                if (targetStatus != EnrollmentStatus.ENROLLED && targetStatus != EnrollmentStatus.CANCELLED) {
                    throw new ConflictException("已联系状态只能变更为已录取或已取消");
                }
                break;
            default:
                throw new ConflictException("不支持的状态转换");
        }
    }

    private EnrollmentStatus parseEnrollmentStatus(String status) {
        try {
            return EnrollmentStatus.fromCode(status);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("报名状态值无效");
        }
    }

    private PaymentStatus parsePaymentStatus(String status) {
        try {
            return PaymentStatus.fromCode(status);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("缴费状态值无效");
        }
    }

    private AdminEnrollmentVO toAdminEnrollmentVO(EnrollmentOrder order) {
        AdminEnrollmentVO vo = new AdminEnrollmentVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setCourseId(order.getCourseId());
        vo.setCourseTitle(order.getCourseTitle());
        vo.setStudentName(order.getStudentName());
        vo.setStudentPhone(order.getStudentPhone());
        vo.setStudentEmail(order.getStudentEmail());
        vo.setRegistrationFee(order.getRegistrationFee());
        vo.setPaidAmount(order.getPaidAmount());
        vo.setPaymentStatus(parsePaymentStatus(order.getPaymentStatus()));
        vo.setEnrollmentStatus(parseEnrollmentStatus(order.getEnrollmentStatus()));
        vo.setCreateTime(format(order.getCreateTime()));
        return vo;
    }

    private String generateOrderNo(LocalDateTime now) {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "EN" + now.format(ORDER_DATE_FORMATTER) + suffix;
    }

    private String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_TIME_FORMATTER);
    }

    private static final int MAX_PAGE_SIZE = 100;

    private long normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
