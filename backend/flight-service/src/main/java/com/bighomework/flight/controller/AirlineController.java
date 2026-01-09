package com.bighomework.flight.controller;

import com.bighomework.common.enums.UserRole;
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
    public ApiResponse<Void> createAirline(
            @RequestBody Airline airline,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (!UserRole.ROLE_PLATFORM_ADMIN.name().equals(role)) {
            return ApiResponse.error(403, "权限不足：仅平台管理员可添加入驻航司");
        }

        if (airlineRepository.existsById(airline.getAirlineCode())) {
            return ApiResponse.error(400, "该航司二字码 (" + airline.getAirlineCode() + ") 已存在");
        }

        airlineRepository.save(airline);
        return ApiResponse.success(null, "航司入驻成功");
    }

    @GetMapping
    public ApiResponse<List<Airline>> getAllAirlines() {
        return ApiResponse.success(airlineRepository.findAll());
    }

    @PutMapping("/{code}/logo")
    public ApiResponse<Void> updateLogo(@PathVariable String code, @RequestParam String logoUrl) {
        Airline airline = airlineRepository.findById(code)
                .orElseThrow(() -> new RuntimeException("航司不存在"));
        airline.setLogoUrl(logoUrl);
        airlineRepository.save(airline);
        return ApiResponse.success(null);
    }

}