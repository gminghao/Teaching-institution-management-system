package com.institution.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.institution.coursemanager.dto.EnrollmentStatusDTO;
import com.institution.coursemanager.dto.EnrollmentSubmitDTO;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.vo.AdminEnrollmentVO;
import com.institution.coursemanager.vo.EnrollmentSubmitVO;
import com.institution.coursemanager.vo.PageResult;

public interface EnrollmentService extends IService<EnrollmentOrder> {

    EnrollmentSubmitVO submitEnrollment(EnrollmentSubmitDTO dto);

    void updateEnrollmentStatus(Long orderId, EnrollmentStatusDTO dto);

    PageResult<AdminEnrollmentVO> getEnrollmentList(
            Integer pageNum,
            Integer pageSize,
            String keyword,
            String paymentStatus,
            String enrollmentStatus);
}
