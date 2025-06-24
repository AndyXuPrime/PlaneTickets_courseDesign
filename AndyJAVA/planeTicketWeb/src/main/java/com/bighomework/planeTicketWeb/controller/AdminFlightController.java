package com.bighomework.planeTicketWeb.controller;

import com.bighomework.planeTicketWeb.dto.admin.FlightAdminDTO;
import com.bighomework.planeTicketWeb.dto.admin.FlightRequest;
import com.bighomework.planeTicketWeb.service.AdminFlightService;
import com.bighomework.planeTicketWeb.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/flights")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 【安全】要求用户具有ADMIN角色才能访问，你需要设置用户角色
public class AdminFlightController {

    private final AdminFlightService adminFlightService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlightAdminDTO>>> getAllFlights() {
        return ResponseEntity.ok(ApiResponse.success(adminFlightService.getAllFlights()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FlightAdminDTO>> createFlight(@Valid @RequestBody FlightRequest request) {
        FlightAdminDTO createdFlight = adminFlightService.createFlight(request);
        return new ResponseEntity<>(ApiResponse.success(createdFlight, "航班创建成功"), HttpStatus.CREATED);
    }

    @PutMapping("/{flightNumber}")
    public ResponseEntity<ApiResponse<FlightAdminDTO>> updateFlight(
            @PathVariable String flightNumber,
            @Valid @RequestBody FlightRequest request) {
        FlightAdminDTO updatedFlight = adminFlightService.updateFlight(flightNumber, request);
        return ResponseEntity.ok(ApiResponse.success(updatedFlight, "航班更新成功"));
    }

    @DeleteMapping("/{flightNumber}")
    public ResponseEntity<ApiResponse<Void>> deleteFlight(@PathVariable String flightNumber) {
        adminFlightService.deleteFlight(flightNumber);
        return ResponseEntity.ok(ApiResponse.success(null, "航班删除成功"));
    }
}