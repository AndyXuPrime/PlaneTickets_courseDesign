package com.bighomework.flight.repository;

import com.bighomework.flight.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String> {

    // 使用 JOIN FETCH 解决 N+1 查询问题，提升性能
    @Query("SELECT f FROM Flight f JOIN FETCH f.airline WHERE f.departureAirport = :dep AND f.arrivalAirport = :arr")
    List<Flight> findByRoute(@Param("dep") String departure, @Param("arr") String arrival);

    // 航司管理员隔离查询
    List<Flight> findByAirline_AirlineCode(String airlineCode);

    @Query("SELECT f FROM Flight f JOIN FETCH f.airline")
    List<Flight> findAllWithAirline();

    @Query("SELECT f FROM Flight f JOIN FETCH f.airline WHERE f.flightNumber LIKE %:no%")
    List<Flight> findByFlightNumberLike(@Param("no") String flightNumber);
}