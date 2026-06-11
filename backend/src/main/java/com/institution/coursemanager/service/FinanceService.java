package com.institution.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.vo.FinanceSummaryVO;

public interface FinanceService extends IService<EnrollmentOrder> {

    FinanceSummaryVO getFinanceSummary();
}
