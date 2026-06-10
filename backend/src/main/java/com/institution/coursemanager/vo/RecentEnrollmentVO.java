package com.institution.coursemanager.vo;

import com.institution.coursemanager.enums.EnrollmentStatus;
import lombok.Data;

@Data
public class RecentEnrollmentVO {
    private String orderNo;
    private String courseTitle;
    private String studentName;
    private EnrollmentStatus enrollmentStatus;
    private String createTime;
}
