package com.bighomework.flight.controller;

import com.bighomework.common.dto.responseDTO.FlightSearchVO;
import com.bighomework.common.enums.UserRole;
import com.bighomework.common.util.ApiResponse;
import com.bighomework.flight.entity.Flight;
import com.bighomework.flight.repository.FlightRepository;
import com.bighomework.flight.service.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final FlightRepository flightRepository;

    @GetMapping("/search")
    public ApiResponse<List<FlightSearchVO>> searchFlights(
            @RequestParam String searchType,
            @RequestParam String value,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate flightDate) {
        return ApiResponse.success(flightService.searchAvailableFlights(searchType, value, flightDate));
    }

    @GetMapping("/admin/list")
    public ApiResponse<List<Flight>> getAdminFlights(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Airline-Code", required = false) String airlineCode) {

        log.info("管理端接口请求: role={}, airlineCode={}", role, airlineCode);

        if (role == null) {
            return ApiResponse.error(401, "未识别的身份，请通过网关访问");
        }

        try {
            // 1. 平台管理员
            if (UserRole.ROLE_PLATFORM_ADMIN.name().equals(role)) {
                return ApiResponse.success(flightRepository.findAllWithAirline());
            }

            // 2. 航司管理员
            if (UserRole.ROLE_AIRLINE_ADMIN.name().equals(role)) {
                if (airlineCode == null || airlineCode.isEmpty()) {
                    return ApiResponse.error(400, "错误：航司管理员未绑定航司代码");
                }
                // 【调用修复后的方法】
                List<Flight> flights = flightRepository.findByAirlineCode(airlineCode);
                log.info("查询到航司 {} 的航班数量: {}", airlineCode, flights.size());
                return ApiResponse.success(flights);
            }

            return ApiResponse.error(403, "权限不足");
        } catch (Exception e) {
            log.error("查询管理端航班列表失败! 错误详情: ", e);
            // 关键：把具体的错误原因返回给前端，方便你直接在浏览器看到
            return ApiResponse.error(500, "数据库查询异常，请检查航班表中的 airline_code 是否在航司表中存在。错误: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ApiResponse<List<Flight>> getFlightStatus(@RequestParam String flightNumber) {
        return ApiResponse.success(flightRepository.findByFlightNumberLike(flightNumber));
    }
    // ================= 用户端：获取推荐/全量航班 =================

    /**
     * 获取全量航班列表（首页推荐使用）
     * @param flightDate 可选日期，不传则默认今天
     */
    @GetMapping("/all")
    public ApiResponse<List<FlightSearchVO>> getAllFlights(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate flightDate) {
        // 逻辑保护：如果前端未传日期，默认使用当前日期计算动态票价
        LocalDate targetDate = (flightDate != null) ? flightDate : LocalDate.now();
        log.info("加载全量推荐航班, 基准日期: {}", targetDate);
        List<FlightSearchVO> flights = flightService.findAllAvailableFlights(targetDate);
        return ApiResponse.success(flights);
    }

    @PutMapping("/{flightNumber}/price")
    public ApiResponse<Void> updatePrice(
            @PathVariable String flightNumber,
            @RequestParam BigDecimal newPrice,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Airline-Code", required = false) String airlineCode) {

        flightService.updateFlightPrice(flightNumber, newPrice, role, airlineCode);
        return ApiResponse.success(null, "价格修改成功");
    }

}