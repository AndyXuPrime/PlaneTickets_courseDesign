package com.bighomework.planeTicketWeb.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
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


    @Query("SELECT t FROM Ticket t " +
           "JOIN FETCH t.flight f " +
           "JOIN FETCH f.airline " +
           "JOIN FETCH t.passenger " +
           "WHERE t.passenger.id = :passengerId ORDER BY t.bookingTime DESC")
    List<Ticket> findByPassengerIdWithAssociations(@Param("passengerId") Integer passengerId);

    @Query("SELECT new com.bighomework.planeTicketWeb.dto.stats.RouteAnalysisDTO(t.flight.departureAirport, t.flight.arrivalAirport, COUNT(t.id)) " +
           "FROM Ticket t " +
           "WHERE t.status IN (com.bighomework.planeTicketWeb.enums.TicketStatus.已支付, com.bighomework.planeTicketWeb.enums.TicketStatus.已使用) " + // 修正点
           "GROUP BY t.flight.departureAirport, t.flight.arrivalAirport " +
           "ORDER BY COUNT(t.id) DESC")
    List<RouteAnalysisDTO> findTopHotRoutes(Pageable pageable);

  
    @Query("SELECT new com.bighomework.planeTicketWeb.dto.stats.RouteSalesDTO(t.flight.departureAirport, t.flight.arrivalAirport, COUNT(t.id), SUM(t.price)) " +
           "FROM Ticket t " +
           "WHERE t.status IN (com.bighomework.planeTicketWeb.enums.TicketStatus.已支付, com.bighomework.planeTicketWeb.enums.TicketStatus.已使用) " +
           "AND t.bookingTime BETWEEN :startDate AND :endDate " +
           "GROUP BY t.flight.departureAirport, t.flight.arrivalAirport " +
           "ORDER BY SUM(t.price) DESC")
    List<RouteSalesDTO> findRouteSalesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    
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

    @Procedure(name = "bookTickets")
    Integer bookTickets(
        @Param("p_customer_id") Integer customerId,
        @Param("p_flight_number") String flightNumber, // 注意：参数名与存储过程一致
        @Param("p_seat_class") String seatClass,
        @Param("p_passenger_ids") String passengerIds
    );

   
    @Procedure(name = "cancelOrder")
    void cancelOrder(@Param("p_order_id") Long orderId);
}