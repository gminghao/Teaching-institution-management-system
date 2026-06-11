package com.institution.coursemanager.controller.admin;

import com.institution.coursemanager.dto.EnrollmentStatusDTO;
import com.institution.coursemanager.service.EnrollmentService;
import com.institution.coursemanager.vo.AdminEnrollmentVO;
import com.institution.coursemanager.vo.PageResult;
import com.institution.coursemanager.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端 - 报名管理
 */
@RestController
@RequestMapping("/api/admin/enrollments")
public class AdminEnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * 报名列表（支持关键字、报名状态、缴费状态筛选）
     */
    @GetMapping
    public Result<PageResult<AdminEnrollmentVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String enrollmentStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(enrollmentService.getEnrollmentList(
                pageNum, pageSize, keyword, paymentStatus, enrollmentStatus));
    }

    /**
     * 更新报名状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody EnrollmentStatusDTO dto) {
        enrollmentService.updateEnrollmentStatus(id, dto);
        return Result.success();
    }
}
