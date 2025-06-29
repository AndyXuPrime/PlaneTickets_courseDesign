package com.bighomework.planeTicketWeb.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bighomework.planeTicketWeb.entity.Airline;
public interface AirlineRepository extends JpaRepository<Airline, String> {

    
}