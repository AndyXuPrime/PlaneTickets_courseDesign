package com.bighomework.planeTicketWeb.controller;

import com.bighomework.planeTicketWeb.dto.responseDTO.FlightSearchVO;
import com.bighomework.planeTicketWeb.service.FlightService;
import com.bighomework.planeTicketWeb.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FlightSearchVO>>> searchFlights(
            @RequestParam String searchType,
            @RequestParam String value,
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

    /**
     * 【核心修改】
     * 此端点现在只通过航班号查询，不再需要日期。
     * 为了与之前的 `/status` 区分并更符合RESTful风格，可以考虑将其路径改为 `/number/{flightNumber}`
     * 但为了最小化改动，我们暂时保留 `/status` 路径，只修改参数。
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<FlightSearchVO>> getFlightStatus(
            @RequestParam String flightNumber) { // 只接收 flightNumber
        // 调用 Service 层对应的新方法
        FlightSearchVO flight = flightService.findFlightByNumber(flightNumber);
        return ResponseEntity.ok(ApiResponse.success(flight));
    }

    // 这个端点可以保留，或者与上面的 /status 合并
    @GetMapping("/{flightNumber}")
    public ResponseEntity<ApiResponse<FlightSearchVO>> getFlightByNumber(
            @PathVariable String flightNumber) {
        FlightSearchVO flight = flightService.findFlightByNumber(flightNumber);
        return ResponseEntity.ok(ApiResponse.success(flight));
    }
}