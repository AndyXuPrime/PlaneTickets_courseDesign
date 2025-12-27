package com.bighomework.admin.controller;

import com.bighomework.admin.repository.StatisticsRepository;
import com.bighomework.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final StatisticsRepository statisticsRepository;

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalRevenue", statisticsRepository.getTotalRevenue());
        data.put("hotRoutes", statisticsRepository.getHotRoutes());
        return ApiResponse.success(data);
    }
}