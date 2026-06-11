package com.institution.coursemanager.controller.admin;

import com.institution.coursemanager.service.FinanceService;
import com.institution.coursemanager.vo.FinanceSummaryVO;
import com.institution.coursemanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端 - 财务统计
 */
@RestController
@RequestMapping("/api/admin/finance")
public class AdminFinanceController {

    @Autowired
    private FinanceService financeService;

    /**
     * 财务汇总
     */
    @GetMapping("/summary")
    public Result<FinanceSummaryVO> summary() {
        return Result.success(financeService.getFinanceSummary());
    }
}
