package com.bighomework.common.feign;

import com.bighomework.common.dto.responseDTO.FlightSearchVO;
import com.bighomework.common.util.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "flight-service", path = "/api/flights")
public interface FlightFeignClient {

    @GetMapping("/{flightNumber}")
    ApiResponse<FlightSearchVO> getFlightByNumber(@PathVariable("flightNumber") String flightNumber);
}