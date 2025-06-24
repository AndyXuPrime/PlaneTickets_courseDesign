package com.bighomework.planeTicketWeb.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bighomework.planeTicketWeb.dto.stats.GenericStatDTO;
import com.bighomework.planeTicketWeb.dto.stats.RouteAnalysisDTO;
import com.bighomework.planeTicketWeb.dto.stats.RouteSalesDTO;
import com.bighomework.planeTicketWeb.entity.Ticket;
import com.bighomework.planeTicketWeb.enums.CabinClass;
import com.bighomework.planeTicketWeb.enums.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT count(t) FROM Ticket t WHERE t.flight.flightNumber = ?1 AND t.flightDate = ?2 AND t.cabinClass = ?3 AND t.status IN ?4")
    int countSoldTickets(String flightNumber, LocalDate flightDate, CabinClass cabinClass, List<TicketStatus> statuses);

    /**
     * 查询最热门的航线
     * @param pageable 用于分页，例如获取Top 10
     * @return 热门航线列表
     */
    // FIX: 在JPQL中引用枚举需要使用全限定名
    @Query("SELECT new com.bighomework.planeTicketWeb.dto.stats.RouteAnalysisDTO(t.flight.departureAirport, t.flight.arrivalAirport, COUNT(t.id)) " +
           "FROM Ticket t " +
           "WHERE t.status IN (com.bighomework.planeTicketWeb.enums.TicketStatus.已支付, com.bighomework.planeTicketWeb.enums.TicketStatus.已使用) " +
           "GROUP BY t.flight.departureAirport, t.flight.arrivalAirport " +
           "ORDER BY COUNT(t.id) DESC")
    List<RouteAnalysisDTO> findTopHotRoutes(Pageable pageable);

    @Query("SELECT new com.bighomework.planeTicketWeb.dto.stats.RouteSalesDTO(t.flight.departureAirport, t.flight.arrivalAirport, COUNT(t.id), SUM(t.finalPrice)) " +
           "FROM Ticket t " +
           "WHERE t.status IN (com.bighomework.planeTicketWeb.enums.TicketStatus.已支付, com.bighomework.planeTicketWeb.enums.TicketStatus.已使用) " +
           "AND t.bookingTime BETWEEN :startDate AND :endDate " +
           "GROUP BY t.flight.departureAirport, t.flight.arrivalAirport " +
           "ORDER BY SUM(t.finalPrice) DESC")
    List<RouteSalesDTO> findRouteSalesBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT new com.bighomework.planeTicketWeb.dto.stats.GenericStatDTO(t.flight.airline.airlineName, COUNT(t.id)) " +
           "FROM Ticket t " +
           "WHERE t.status IN (com.bighomework.planeTicketWeb.enums.TicketStatus.已支付, com.bighomework.planeTicketWeb.enums.TicketStatus.已使用) " +
           "GROUP BY t.flight.airline.airlineName " +
           "ORDER BY COUNT(t.id) DESC")
    List<GenericStatDTO> findStatsByAirline();
    
    @Query("SELECT new com.bighomework.planeTicketWeb.dto.stats.GenericStatDTO(t.flight.aircraftModel, COUNT(t.id)) " +
           "FROM Ticket t " +
           "WHERE t.status IN (com.bighomework.planeTicketWeb.enums.TicketStatus.已支付, com.bighomework.planeTicketWeb.enums.TicketStatus.已使用) " +
           "GROUP BY t.flight.aircraftModel " +
           "ORDER BY COUNT(t.id) DESC")
    List<GenericStatDTO> findStatsByAircraftModel();

}