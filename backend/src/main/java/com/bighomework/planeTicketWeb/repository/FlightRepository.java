package com.bighomework.planeTicketWeb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bighomework.planeTicketWeb.entity.Flight;

import jakarta.persistence.LockModeType;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String> {


    @Query("SELECT f FROM Flight f JOIN FETCH f.airline WHERE f.departureAirport = :departure AND f.arrivalAirport = :arrival")
    List<Flight> findByDepartureAirportAndArrivalAirport(@Param("departure") String departure, @Param("arrival") String arrival);


    @Query("SELECT f FROM Flight f JOIN FETCH f.airline a WHERE a.airlineCode = :airlineCode")
    List<Flight> findByAirlineAirlineCode(@Param("airlineCode") String airlineCode);


    @Query("SELECT f FROM Flight f JOIN FETCH f.airline")
    List<Flight> findAllWithAirline();



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flight f WHERE f.flightNumber = :flightNumber")
    Optional<Flight> findByIdWithLock(@Param("flightNumber") String flightNumber);


    @Query("SELECT f FROM Flight f JOIN FETCH f.airline WHERE f.flightNumber = :flightNumber")
    Optional<Flight> findByIdAndFetchAirline(@Param("flightNumber") String flightNumber);

}