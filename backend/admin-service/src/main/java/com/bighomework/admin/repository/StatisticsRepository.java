package com.bighomework.admin.repository;

import com.bighomework.admin.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT SUM(t.price) FROM Ticket t WHERE t.status = '已支付' OR t.status = '已使用'")
    BigDecimal getTotalRevenue();

    @Query(value = "SELECT flight_number, COUNT(*) as count FROM tickets GROUP BY flight_number ORDER BY count DESC LIMIT 5", nativeQuery = true)
    List<Object[]> getHotRoutes();
}