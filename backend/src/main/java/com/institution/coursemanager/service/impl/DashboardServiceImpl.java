package com.institution.coursemanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.institution.coursemanager.entity.Course;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.enums.CourseStatus;
import com.institution.coursemanager.enums.EnrollmentStatus;
import com.institution.coursemanager.enums.PaymentStatus;
import com.institution.coursemanager.mapper.CourseMapper;
import com.institution.coursemanager.mapper.EnrollmentOrderMapper;
import com.institution.coursemanager.service.DashboardService;
import com.institution.coursemanager.vo.DashboardOverviewVO;
import com.institution.coursemanager.vo.RecentEnrollmentVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl extends ServiceImpl<EnrollmentOrderMapper, EnrollmentOrder>
        implements DashboardService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CourseMapper courseMapper;

    public DashboardServiceImpl(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    @Override
    public DashboardOverviewVO getOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setTotalCourses(courseMapper.selectCount(new LambdaQueryWrapper<Course>()));
        vo.setOnlineCourses(courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, CourseStatus.ONLINE.getCode())));
        vo.setTotalEnrollments(count());
        vo.setPendingEnrollments(count(new LambdaQueryWrapper<EnrollmentOrder>()
                .eq(EnrollmentOrder::getEnrollmentStatus, EnrollmentStatus.PENDING.getCode())));
        vo.setPaidCount(countByPaymentStatus(PaymentStatus.PAID));
        vo.setUnpaidCount(countByPaymentStatus(PaymentStatus.UNPAID));
        vo.setPartialCount(countByPaymentStatus(PaymentStatus.PARTIAL));
        vo.setRefundedCount(countByPaymentStatus(PaymentStatus.REFUNDED));
        vo.setRecentEnrollments(getRecentEnrollments());
        return vo;
    }

    private Long countByPaymentStatus(PaymentStatus status) {
        return count(new LambdaQueryWrapper<EnrollmentOrder>()
                .eq(EnrollmentOrder::getPaymentStatus, status.getCode()));
    }

    private List<RecentEnrollmentVO> getRecentEnrollments() {
        return list(new LambdaQueryWrapper<EnrollmentOrder>()
                        .orderByDesc(EnrollmentOrder::getCreateTime)
                        .last("LIMIT 5"))
                .stream()
                .map(this::toRecentEnrollmentVO)
                .toList();
    }

    private RecentEnrollmentVO toRecentEnrollmentVO(EnrollmentOrder order) {
        RecentEnrollmentVO vo = new RecentEnrollmentVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setCourseTitle(order.getCourseTitle());
        vo.setStudentName(order.getStudentName());
        vo.setEnrollmentStatus(EnrollmentStatus.fromCode(order.getEnrollmentStatus()));
        vo.setCreateTime(format(order.getCreateTime()));
        return vo;
    }

    private String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_TIME_FORMATTER);
    }
}
