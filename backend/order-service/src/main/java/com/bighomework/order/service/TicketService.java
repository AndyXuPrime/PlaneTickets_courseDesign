package com.bighomework.order.service;

import com.bighomework.common.dto.requestDTO.BookingRequest;
import com.bighomework.common.dto.responseDTO.LogicalOrderVO;
import com.bighomework.common.dto.responseDTO.TicketVO;
import com.bighomework.common.enums.TicketStatus;
import com.bighomework.order.entity.Ticket;
import com.bighomework.order.entity.TicketStatusLog;

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

    /**
     * 管理员搜索机票
     * @param flightNumber 航班号 (可选)
     * @param status 状态 (可选)
     */
    List<Ticket> searchTickets(String flightNumber, TicketStatus status);

    /**
     * 管理员办理登机 (核销)
     * @param ticketId 机票ID
     */
    void checkInTicket(Long ticketId);

    /**
     * 获取机票的操作日志
     * @param ticketId 机票ID
     */
    List<TicketStatusLog> getTicketLogs(Long ticketId);
}