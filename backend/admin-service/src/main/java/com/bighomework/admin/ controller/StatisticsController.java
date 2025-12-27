package com.bighomework.admin.controller;

import com.bighomework.admin.service.AdminStatService;
import com.bighomework.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final AdminStatService adminStatService;

    @GetMapping("/customer-analysis")
    public ApiResponse<Map<String, Object>> getCustomerAnalysis(@RequestParam String dimension) {
        return ApiResponse.success(adminStatService.analyzeCustomersByDimension(dimension));
    }

    @GetMapping("/route-analysis")
    public ApiResponse<Map<String, Object>> getRouteAnalysis() {
        return ApiResponse.success(adminStatService.analyzeRoutes());
    }
}