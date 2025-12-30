package com.bighomework.flight.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.bighomework.common.dto.requestDTO.FlightRequest;
import com.bighomework.common.dto.responseDTO.FlightSearchVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

public interface FlightService {
    List<FlightSearchVO> searchAvailableFlights(String searchType, String value, LocalDate date);
    List<FlightSearchVO> findAllAvailableFlights(LocalDate date);
    List<FlightSearchVO> findFlightByNumber(String flightNumber);
    void updateFlightPrice(String flightNumber, BigDecimal newPrice, String role, String adminAirlineCode);
    @Transactional(readOnly = true)
    FlightSearchVO findFlightByNumberAndDate(String flightNumber, LocalDate date);

    @CacheEvict(value = "flight_search", allEntries = true)
    void clearFlightCache();

    void saveFlight(FlightRequest request);
}