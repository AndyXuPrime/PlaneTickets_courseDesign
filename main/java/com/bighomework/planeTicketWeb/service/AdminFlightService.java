package com.bighomework.planeTicketWeb.service;

import com.bighomework.planeTicketWeb.dto.admin.FlightAdminDTO;
import com.bighomework.planeTicketWeb.dto.admin.FlightRequest;
import java.util.List;

public interface AdminFlightService {
    List<FlightAdminDTO> getAllFlights();
    FlightAdminDTO createFlight(FlightRequest request);
    FlightAdminDTO updateFlight(String flightNumber, FlightRequest request);
    void deleteFlight(String flightNumber);
}