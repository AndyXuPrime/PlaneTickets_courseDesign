package com.bighomework.admin.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// 【修改点】指向 common 模块中的正确路径
import com.bighomework.common.dto.stats.GenericStatDTO;

public interface AdminStatService {
    Map<String, Object> analyzeCustomersByDimension(String dimension);
    Map<String, Object> analyzeRoutes();
    Map<String, Object> getRouteSalesAnalysis(LocalDate startDate, LocalDate endDate);
    List<GenericStatDTO> getStatsReport(String dimension);
}