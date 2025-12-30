package com.bighomework.order.repository;

import com.bighomework.common.enums.TicketStatus;
import com.bighomework.order.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // 用户端：查询我的订单
    List<Ticket> findByUserId(Integer userId);

    // 管理端：组合查询
    List<Ticket> findByFlightNumberAndStatus(String flightNumber, TicketStatus status);

    List<Ticket> findByFlightNumber(String flightNumber);

    List<Ticket> findByStatus(TicketStatus status);
}