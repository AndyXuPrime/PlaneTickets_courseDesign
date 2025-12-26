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

@Slf4j
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    /**
     * 搜索航班 (支持按航线或航司)
     * 例如: GET /api/flights/search?searchType=byRoute&value=PEK-SHA&flightDate=2023-12-01
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FlightSearchVO>>> searchFlights(
            @RequestParam String searchType,
            @RequestParam String value,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate flightDate) {

        log.info("收到航班搜索请求: type={}, value={}, date={}", searchType, value, flightDate);
        List<FlightSearchVO> flights = flightService.searchAvailableFlights(searchType, value, flightDate);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    /**
     * 获取某日所有航班 (用于管理员查看或首页展示)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<FlightSearchVO>>> getAllFlights(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate flightDate) {

        List<FlightSearchVO> flights = flightService.findAllAvailableFlights(flightDate);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    /**
     * 根据航班号模糊查询 (用于状态查询或搜索框联想)
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<FlightSearchVO>>> getFlightStatus(
            @RequestParam String flightNumber) {

        List<FlightSearchVO> flights = flightService.findFlightByNumber(flightNumber);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    /**
     * 根据航班号精确查询 (供 Feign 调用，返回单个对象)
     * 注意：这里简化处理，返回列表中的第一个航班。
     * 实际业务中应该传日期来精确定位。
     */
    @GetMapping("/{flightNumber}")
    public ResponseEntity<ApiResponse<FlightSearchVO>> getFlightByNumber(
            @PathVariable String flightNumber) {

        List<FlightSearchVO> flights = flightService.findFlightByNumber(flightNumber);

        if (flights.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("航班不存在"));
        }

        // 返回第一个匹配的航班
        return ResponseEntity.ok(ApiResponse.success(flights.get(0)));
    }
}