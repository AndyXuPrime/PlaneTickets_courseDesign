package com.bighomework.order.service;

import com.bighomework.common.dto.requestDTO.BookingRequest;
import com.bighomework.common.dto.responseDTO.LogicalOrderVO;
import com.bighomework.common.dto.responseDTO.TicketVO;

import java.util.List;

public interface TicketService {

    /**
     * 创建订单
     * @param request 预订请求
     * @param username 预订人用户名 (手机号)
     * @return 创建成功的机票列表
     */
    List<TicketVO> createTickets(BookingRequest request, String username);

    /**
     * 退票
     * @param ticketId 机票ID
     * @param username 操作人用户名 (用于校验权限)
     */
    void refundTicket(Long ticketId, String username);

    /**
     * 查询我的订单
     * @param username 用户名
     * @return 逻辑订单列表 (按预订时间分组)
     */
    List<LogicalOrderVO> getMyOrders(String username);
}