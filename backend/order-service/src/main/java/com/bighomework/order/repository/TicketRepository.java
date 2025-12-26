package com.bighomework.order.repository;

import com.bighomework.common.enums.CabinClass;
import com.bighomework.common.enums.TicketStatus;
import com.bighomework.order.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // 【修改】不再通过 t.flight.flightNumber 导航，直接查 t.flightNumber
    @Query("SELECT count(t) FROM Ticket t WHERE t.flightNumber = ?1 AND t.flightDate = ?2 AND t.cabinClass = ?3 AND t.status IN ?4")
    int countSoldTickets(String flightNumber, LocalDate flightDate, CabinClass cabinClass, List<TicketStatus> statuses);

    // 【修改】移除 JOIN FETCH，只查 Ticket 本身
    @Query("SELECT t FROM Ticket t WHERE t.userId = :userId ORDER BY t.bookingTime DESC")
    List<Ticket> findByUserId(Integer userId);

    // --- 统计类查询暂时注释，建议迁移到 Admin 服务或通过数据同步解决 ---
    /*
    @Query("SELECT new com.bighomework.planeTicketWeb.dto.stats.RouteAnalysisDTO(...) ...")
    List<RouteAnalysisDTO> findTopHotRoutes(Pageable pageable);
    */
}