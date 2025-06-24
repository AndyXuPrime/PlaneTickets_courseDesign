package com.bighomework.planeTicketWeb.service;


import java.util.List;

import com.bighomework.planeTicketWeb.dto.requestDTO.OrderRequest;
import com.bighomework.planeTicketWeb.dto.responseDTO.OrderDetailVO; // 确保导入 List
public interface OrderService {
    /**
     * 创建订单
     * @param orderRequest 订单请求 DTO
     * @param bookerId 下单用户ID
     * @return 订单详情 VO
     */
    OrderDetailVO createOrder(OrderRequest orderRequest, Integer bookerId);

    /**
     * 根据ID查询订单详情
     * @param orderId 订单ID
     * @return 订单详情 VO
     */
    OrderDetailVO findOrderDetailsById(Long orderId);
    /**
     * 【新增方法】
     * 根据用户名（手机号）查询该用户的所有订单。
     * 用于“我的订单”页面。
     * @param username 用户名，在此系统中对应客户的手机号
     * @return 该用户所有订单的详情列表
     */
    List<OrderDetailVO> findOrdersByUsername(String username);
    /**
     * 申请退票
     * @param ticketId 机票ID
     * @param operator 操作人标识
     */
    void refundTicket(Long ticketId, String operator);
     void cancelOrder(Long orderId, String username); 
}