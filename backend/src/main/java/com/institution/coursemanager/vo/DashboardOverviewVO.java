package com.institution.coursemanager.vo;

import java.util.List;
import lombok.Data;

@Data
public class DashboardOverviewVO {
    private Long totalCourses;
    private Long onlineCourses;
    private Long totalEnrollments;
    private Long pendingEnrollments;
    private Long paidCount;
    private Long unpaidCount;
    private Long partialCount;
    private Long refundedCount;
    private List<RecentEnrollmentVO> recentEnrollments;
}
