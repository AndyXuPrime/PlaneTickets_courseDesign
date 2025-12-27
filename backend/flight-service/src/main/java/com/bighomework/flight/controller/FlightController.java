package com.bighomework.flight.controller;

import com.bighomework.common.dto.responseDTO.FlightSearchVO;
import com.bighomework.common.util.ApiResponse;
import com.bighomework.flight.service.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
// ... 保留原有 import
import com.bighomework.flight.entity.Flight;
import com.bighomework.flight.repository.FlightRepository;
import com.bighomework.common.enums.UserRole;
import com.bighomework.common.exception.BusinessException;
@Slf4j
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final FlightRepository flightRepository; // 直接注入 Repository 方便管理端查询

    // ================= 已有跑通接口 (保持不动) =================

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FlightSearchVO>>> searchFlights(
            @RequestParam String searchType, @RequestParam String value,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate flightDate) {
        List<FlightSearchVO> flights = flightService.searchAvailableFlights(searchType, value, flightDate);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<FlightSearchVO>>> getAllFlights(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate flightDate) {
        List<FlightSearchVO> flights = flightService.findAllAvailableFlights(flightDate);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<ApiResponse<FlightSearchVO>> getFlightByNumber(@PathVariable String flightNumber) {
        List<FlightSearchVO> flights = flightService.findFlightByNumber(flightNumber);
        if (flights.isEmpty()) return ResponseEntity.ok(ApiResponse.error("航班不存在"));
        return ResponseEntity.ok(ApiResponse.success(flights.get(0)));
    }

    // ================= 新增管理接口 (支持数据隔离) =================

    /**
     * 【管理后台专用】获取航班列表
     * 逻辑：平台管理员看全部，航司管理员只能看自己家的
     */
    @GetMapping("/admin/list")
    public ApiResponse<List<Flight>> getAdminFlights(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Airline-Code", required = false) String airlineCode) {

        log.info("管理端查询航班: role={}, airlineCode={}", role, airlineCode);

        if (UserRole.ROLE_PLATFORM_ADMIN.name().equals(role)) {
            // 1. 平台管理员：查询所有航班
            return ApiResponse.success(flightRepository.findAll());
        }

        if (UserRole.ROLE_AIRLINE_ADMIN.name().equals(role)) {
            // 2. 航司管理员：必须有航司代码，且只能查自己的
            if (airlineCode == null || airlineCode.isEmpty()) {
                throw new BusinessException("航司管理员缺少所属航司信息");
            }
            return ApiResponse.success(flightRepository.findByAirlineAirlineCode(airlineCode));
        }

        throw new BusinessException("权限不足，无法访问管理列表");
    }
}