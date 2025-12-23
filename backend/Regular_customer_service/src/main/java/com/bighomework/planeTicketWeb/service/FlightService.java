package com.bighomework.planeTicketWeb.service;

import java.time.LocalDate;
import java.util.List;

import com.bighomework.planeTicketWeb.dto.responseDTO.FlightSearchVO;

public interface FlightService {
    List<FlightSearchVO> searchAvailableFlights(String searchType, String value, LocalDate date);
    List<FlightSearchVO> findAllAvailableFlights(LocalDate date);
    FlightSearchVO findFlightByNumberAndDate(String flightNumber, LocalDate date);

    List<FlightSearchVO> findFlightByNumber(String flightNumber);
}