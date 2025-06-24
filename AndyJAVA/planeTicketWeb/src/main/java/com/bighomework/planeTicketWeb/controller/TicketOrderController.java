package com.bighomework.planeTicketWeb.controller;

import java.util.List;

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

import com.bighomework.planeTicketWeb.dto.requestDTO.OrderRequest;
import com.bighomework.planeTicketWeb.dto.responseDTO.OrderDetailVO;
import com.bighomework.planeTicketWeb.service.OrderService;
import com.bighomework.planeTicketWeb.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class TicketOrderController {

    // FIX: 移除 '= null'
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDetailVO>> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        Integer mockBookerId = 1; // 模拟登录用户ID
        OrderDetailVO createdOrder = orderService.createOrder(orderRequest, mockBookerId);
        return new ResponseEntity<>(ApiResponse.success(createdOrder, "订单创建成功，请尽快支付"), HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailVO>> getOrderDetails(@PathVariable Long orderId) {
        OrderDetailVO orderDetail = orderService.findOrderDetailsById(orderId);
        return ResponseEntity.ok(ApiResponse.success(orderDetail));
    }
    
    @PostMapping("/{ticketId}/refund")
    public ResponseEntity<ApiResponse<Void>> refundTicket(@PathVariable Long ticketId) {
        String operator = "customer"; // 模拟操作人
        orderService.refundTicket(ticketId, operator);
        return ResponseEntity.ok(ApiResponse.success(null, "退票申请已提交"));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderDetailVO>>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        // 我们假设从 token 中获取的用户名就是用户的手机号
        String username = userDetails.getUsername(); 
        List<OrderDetailVO> orders = orderService.findOrdersByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        orderService.cancelOrder(orderId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "订单取消成功"));
    }
}