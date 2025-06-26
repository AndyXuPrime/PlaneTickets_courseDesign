package com.bighomework.planeTicketWeb.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bighomework.planeTicketWeb.dto.requestDTO.BookingRequest;
import com.bighomework.planeTicketWeb.dto.responseDTO.LogicalOrderVO;
import com.bighomework.planeTicketWeb.dto.responseDTO.TicketVO;
import com.bighomework.planeTicketWeb.entity.Customer;
import com.bighomework.planeTicketWeb.exception.BusinessException;
import com.bighomework.planeTicketWeb.repository.CustomerRepository;
import com.bighomework.planeTicketWeb.service.TicketService;
import com.bighomework.planeTicketWeb.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/bookings") 
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private CustomerRepository customerRepository;


    @PostMapping
    public ResponseEntity<ApiResponse<List<TicketVO>>> createBooking(
            @Valid 
            @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("createBooking-request:{}",request);
        log.info("createBooking-userDetails:{}",userDetails);

        Customer booker = customerRepository.findByPhone(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("预订人信息不存在"));
        log.info("booker:{}",booker);
        List<TicketVO> createdTickets = ticketService.createTickets(request, booker.getCustomerId());
        
        return new ResponseEntity<>(ApiResponse.success(createdTickets, "预订成功"), HttpStatus.CREATED);
    }


    @GetMapping("/my-tickets")
    public ResponseEntity<ApiResponse<List<LogicalOrderVO>>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<LogicalOrderVO> orders = ticketService.getMyOrders(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    
    @PostMapping("/tickets/{ticketId}/refund")
    public ResponseEntity<ApiResponse<Void>> refundTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ticketService.refundTicket(ticketId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "退票申请已提交"));
    }
}