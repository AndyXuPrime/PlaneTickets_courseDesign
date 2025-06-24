package com.bighomework.planeTicketWeb.service;

import java.time.LocalDate;
import java.util.List;

import com.bighomework.planeTicketWeb.dto.responseDTO.FlightSearchVO;

public interface FlightService {
    // 【修改方法签名】
    // 将其变得更通用，可以接受不同的查询参数
    List<FlightSearchVO> searchAvailableFlights(String searchType, String value, LocalDate date);
    List<FlightSearchVO> findAllAvailableFlights(LocalDate date); 
    FlightSearchVO findFlightByNumberAndDate(String flightNumber, LocalDate date); 
    FlightSearchVO findFlightByNumber(String flightNumber);
}