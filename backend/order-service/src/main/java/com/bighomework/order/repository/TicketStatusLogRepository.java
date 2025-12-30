package com.bighomework.order.repository;

import com.bighomework.order.entity.TicketStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketStatusLogRepository extends JpaRepository<TicketStatusLog, Integer> {

    List<TicketStatusLog> findByTicketTicketIdOrderByChangeTimeDesc(Long ticketId);
}