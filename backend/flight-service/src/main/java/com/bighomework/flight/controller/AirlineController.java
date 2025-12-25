package com.bighomework.flight.controller;

import com.bighomework.common.util.ApiResponse;
import com.bighomework.flight.entity.Airline;
import com.bighomework.flight.repository.AirlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airlines")
@RequiredArgsConstructor
public class AirlineController {

    private final AirlineRepository airlineRepository;

    @PostMapping
    public ApiResponse<Airline> createAirline(@RequestBody Airline airline) {
        return ApiResponse.success(airlineRepository.save(airline));
    }

    @GetMapping
    public ApiResponse<List<Airline>> getAllAirlines() {
        return ApiResponse.success(airlineRepository.findAll());
    }

    // 航司管理员更新自己的 Logo
    @PutMapping("/{code}/logo")
    public ApiResponse<Void> updateLogo(@PathVariable String code, @RequestParam String logoUrl) {
        Airline airline = airlineRepository.findById(code)
                .orElseThrow(() -> new RuntimeException("航司不存在"));
        airline.setLogoUrl(logoUrl);
        airlineRepository.save(airline);
        return ApiResponse.success(null);
    }
}