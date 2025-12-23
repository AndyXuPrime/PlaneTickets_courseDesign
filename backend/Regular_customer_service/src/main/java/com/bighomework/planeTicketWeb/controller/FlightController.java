package com.bighomework.planeTicketWeb.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bighomework.planeTicketWeb.dto.responseDTO.FlightSearchVO;
import com.bighomework.planeTicketWeb.service.FlightService;
import com.bighomework.planeTicketWeb.util.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {
    @Autowired
    private FlightService flightService;

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


    @GetMapping("/status")
    // 【修改】返回类型改为 List<FlightSearchVO>
    public ResponseEntity<ApiResponse<List<FlightSearchVO>>> getFlightStatus(
            @RequestParam String flightNumber) {

        List<FlightSearchVO> flights = flightService.findFlightByNumber(flightNumber);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }


    @GetMapping("/{flightNumber}")
    // 【修改】返回类型改为 List<FlightSearchVO>
    public ResponseEntity<ApiResponse<List<FlightSearchVO>>> getFlightByNumber(
            @PathVariable String flightNumber) {
        List<FlightSearchVO> flights = flightService.findFlightByNumber(flightNumber);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }
}