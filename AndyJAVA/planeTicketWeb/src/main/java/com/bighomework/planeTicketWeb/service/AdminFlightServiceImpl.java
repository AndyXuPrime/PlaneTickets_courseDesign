package com.bighomework.planeTicketWeb.service;

import com.bighomework.planeTicketWeb.dto.admin.FlightAdminDTO;
import com.bighomework.planeTicketWeb.dto.admin.FlightRequest;
import com.bighomework.planeTicketWeb.entity.Airline;
import com.bighomework.planeTicketWeb.entity.Flight;
import com.bighomework.planeTicketWeb.exception.BusinessException;
import com.bighomework.planeTicketWeb.repository.AirlineRepository;
import com.bighomework.planeTicketWeb.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminFlightServiceImpl implements AdminFlightService {

    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository; // 需要注入 AirlineRepository

    @Override
    public List<FlightAdminDTO> getAllFlights() {
        return flightRepository.findAllWithAirline().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FlightAdminDTO createFlight(FlightRequest request) {
        if (flightRepository.existsById(request.getFlightNumber())) {
            throw new BusinessException("航班号 " + request.getFlightNumber() + " 已存在。");
        }
        Flight flight = new Flight();
        updateFlightEntityFromRequest(flight, request);
        Flight savedFlight = flightRepository.save(flight);
        return convertToDto(savedFlight);
    }

    @Override
    @Transactional
    public FlightAdminDTO updateFlight(String flightNumber, FlightRequest request) {
        Flight flight = flightRepository.findById(flightNumber)
                .orElseThrow(() -> new BusinessException("航班号 " + flightNumber + " 不存在。"));
        updateFlightEntityFromRequest(flight, request);
        Flight updatedFlight = flightRepository.save(flight);
        return convertToDto(updatedFlight);
    }

    @Override
    @Transactional
    public void deleteFlight(String flightNumber) {
        if (!flightRepository.existsById(flightNumber)) {
            throw new BusinessException("航班号 " + flightNumber + " 不存在。");
        }
        // 注意：如果已有订单关联此航班，直接删除可能会失败。需要更复杂的逻辑（如软删除）。
        flightRepository.deleteById(flightNumber);
    }

    private void updateFlightEntityFromRequest(Flight flight, FlightRequest request) {
        Airline airline = airlineRepository.findById(request.getAirlineCode())
                .orElseThrow(() -> new BusinessException("航空公司代码 " + request.getAirlineCode() + " 不存在。"));
        
        flight.setFlightNumber(request.getFlightNumber());
        flight.setAirline(airline);
        flight.setDepartureAirport(request.getDepartureAirport());
        flight.setArrivalAirport(request.getArrivalAirport());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setAircraftModel(request.getAircraftModel());
        flight.setBusinessSeats(request.getBusinessSeats());
        flight.setEconomySeats(request.getEconomySeats());
        flight.setBasePrice(request.getBasePrice());
    }
    
    private FlightAdminDTO convertToDto(Flight flight) {
        FlightAdminDTO dto = new FlightAdminDTO();
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setAirlineName(flight.getAirline().getAirlineName());
        dto.setAirlineCode(flight.getAirline().getAirlineCode());
        dto.setDepartureAirport(flight.getDepartureAirport());
        dto.setArrivalAirport(flight.getArrivalAirport());
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setArrivalTime(flight.getArrivalTime());
        dto.setAircraftModel(flight.getAircraftModel());
        dto.setBusinessSeats(flight.getBusinessSeats());
        dto.setEconomySeats(flight.getEconomySeats());
        dto.setBasePrice(flight.getBasePrice());
        return dto;
    }
}