package com.bighomework.flight.repository;

import com.bighomework.flight.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String> {

    /**
     * 用户端：按航线查询并预加载航司信息
     */
    @Query("SELECT f FROM Flight f JOIN FETCH f.airline WHERE f.departureAirport = :dep AND f.arrivalAirport = :arr")
    List<Flight> findByRoute(@Param("dep") String departure, @Param("arr") String arrival);

    /**
     * 【核心修复】航司管理员：按航司代码查询并强制立即加载航司对象
     * 解决航司管理员访问列表时的 500 懒加载异常
     */
    @Query("SELECT f FROM Flight f JOIN FETCH f.airline a WHERE a.airlineCode = :airlineCode")
    List<Flight> findByAirlineCode(@Param("airlineCode") String airlineCode);

    /**
     * 平台管理员：查询全量航班并预加载航司信息
     */
    @Query("SELECT f FROM Flight f JOIN FETCH f.airline")
    List<Flight> findAllWithAirline();

    /**
     * 模糊搜索：用于航班动态查询
     */
    @Query("SELECT f FROM Flight f JOIN FETCH f.airline WHERE f.flightNumber LIKE %:no%")
    List<Flight> findByFlightNumberLike(@Param("no") String flightNumber);
}