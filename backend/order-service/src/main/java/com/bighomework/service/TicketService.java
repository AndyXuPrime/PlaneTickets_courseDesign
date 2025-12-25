package com.bighomework.planeTicketWeb.service;

import java.util.List;

import com.bighomework.planeTicketWeb.dto.requestDTO.BookingRequest;
import com.bighomework.planeTicketWeb.dto.responseDTO.LogicalOrderVO;
import com.bighomework.planeTicketWeb.dto.responseDTO.TicketVO;

public interface TicketService {
    List<TicketVO> createTickets(BookingRequest request, Integer bookerId);
    void refundTicket(Long ticketId, String username);
    List<LogicalOrderVO> getMyOrders(String username);
}