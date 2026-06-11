package com.institution.coursemanager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.institution.coursemanager.entity.EnrollmentOrder;
import com.institution.coursemanager.vo.DashboardOverviewVO;

public interface DashboardService extends IService<EnrollmentOrder> {

    DashboardOverviewVO getOverview();
}
