package com.bighomework.order.controller;

import com.bighomework.common.dto.requestDTO.BookingRequest;
import com.bighomework.common.dto.responseDTO.LogicalOrderVO;
import com.bighomework.common.dto.responseDTO.TicketVO;
import com.bighomework.common.util.ApiResponse;
import com.bighomework.order.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<TicketVO>>> createBooking(
            @RequestBody BookingRequest request,
            @RequestHeader("X-User-Name") String username) { // 从 Gateway 传来的 Header 获取用户名

        log.info("收到下单请求: user={}, flight={}", username, request.getFlightNumber());

        // 调用 Service，传入用户名 (Service 内部会去 Auth 服务查 ID)
        List<TicketVO> createdTickets = ticketService.createTickets(request, username);

        return new ResponseEntity<>(ApiResponse.success(createdTickets, "预订成功"), HttpStatus.CREATED);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<ApiResponse<List<LogicalOrderVO>>> getMyOrders(
            @RequestHeader("X-User-Name") String username) {

        List<LogicalOrderVO> orders = ticketService.getMyOrders(username);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/tickets/{ticketId}/refund")
    public ResponseEntity<ApiResponse<Void>> refundTicket(
            @PathVariable Long ticketId,
            @RequestHeader("X-User-Name") String username) {

        ticketService.refundTicket(ticketId, username);
        return ResponseEntity.ok(ApiResponse.success(null, "退票申请已提交"));
    }
}