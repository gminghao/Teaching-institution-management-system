package com.institution.coursemanager.controller.visitor;

import com.institution.coursemanager.dto.EnrollmentSubmitDTO;
import com.institution.coursemanager.service.EnrollmentService;
import com.institution.coursemanager.vo.EnrollmentSubmitVO;
import com.institution.coursemanager.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访客端 - 课程报名
 */
@RestController
@RequestMapping("/api/public/enrollments")
public class PublicEnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * 提交课程报名
     */
    @PostMapping
    public Result<EnrollmentSubmitVO> submit(@Valid @RequestBody EnrollmentSubmitDTO dto) {
        return Result.success(enrollmentService.submitEnrollment(dto));
    }
}
