package com.bighomework.planeTicketWeb.service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.bighomework.planeTicketWeb.dto.stats.GenericStatDTO;

public interface AdminStatService {
    Map<String, Object> analyzeCustomersByDimension(String dimension);
    Map<String, Object> analyzeRoutes();
    Map<String, Object> getRouteSalesAnalysis(LocalDate startDate, LocalDate endDate);
    List<GenericStatDTO> getStatsReport(String dimension);
}