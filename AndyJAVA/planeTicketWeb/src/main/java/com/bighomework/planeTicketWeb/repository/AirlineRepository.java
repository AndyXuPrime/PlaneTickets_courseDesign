// AirlineRepository.java
package com.bighomework.planeTicketWeb.repository;
import com.bighomework.planeTicketWeb.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AirlineRepository extends JpaRepository<Airline, String> {

    
}