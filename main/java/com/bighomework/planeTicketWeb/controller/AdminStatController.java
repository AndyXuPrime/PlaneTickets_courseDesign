package com.bighomework.planeTicketWeb.controller;

import com.bighomework.planeTicketWeb.dto.stats.GenericStatDTO;
import com.bighomework.planeTicketWeb.service.AdminStatService;
import com.bighomework.planeTicketWeb.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatController {

    private final AdminStatService adminStatService;
    
    // 原有的 AdminController 中的方法可以移到这里，保持职责单一
    @GetMapping("/customer-analysis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomerAnalysis(@RequestParam String dimension) {
        Map<String, Object> result = adminStatService.analyzeCustomersByDimension(dimension);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/route-analysis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopRoutes() {
        Map<String, Object> result = adminStatService.analyzeRoutes();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 新增的统计API
    @GetMapping("/route-sales")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRouteSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> result = adminStatService.getRouteSalesAnalysis(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<GenericStatDTO>>> getGenericReport(@RequestParam String dimension) {
        List<GenericStatDTO> result = adminStatService.getStatsReport(dimension);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}