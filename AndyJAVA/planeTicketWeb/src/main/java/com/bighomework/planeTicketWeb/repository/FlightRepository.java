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

    /**
     * 【推荐】使用方法名派生查询，并结合 @EntityGraph 来解决 N+1 问题。
     * 这比手写 @Query 更安全、更清晰。
     * @EntityGraph 会告诉JPA在执行查询时，立即（EAGER）加载指定的关联实体。
     */
    // @EntityGraph(attributePaths = {"airline"}) // 如果 Flight 实体中有 @NamedEntityGraph 定义，则用这个
    // 如果没有，可以直接在 @Query 中使用 JOIN FETCH
    @Query("SELECT f FROM Flight f JOIN FETCH f.airline WHERE f.departureAirport = :departure AND f.arrivalAirport = :arrival")
    List<Flight> findByDepartureAirportAndArrivalAirport(@Param("departure") String departure, @Param("arrival") String arrival);


    @Query("SELECT f FROM Flight f JOIN FETCH f.airline a WHERE a.airlineCode = :airlineCode")
    List<Flight> findByAirlineAirlineCode(@Param("airlineCode") String airlineCode);

    /**
     * 【核心修改】移除对 findAll() 的重写。
     * 如果需要在所有航班查询中都附带航空公司信息，我们应该在调用方（Service层）处理，
     * 或者定义一个全新的方法，而不是重写框架方法。
     * 例如，可以定义一个 findAllWithAirline() 方法。
     */
    @Query("SELECT f FROM Flight f JOIN FETCH f.airline")
    List<Flight> findAllWithAirline();


    /**
     * 【核心修改】合并了 findByIdWithAirline 和 findByIdWithLock。
     * JpaRepository 已经提供了 findById(ID id)。如果需要加锁，可以在Service层使用 @Transactional 和 EntityManager 来实现。
     * 为了简化，我们只保留一个最常用的带锁查询，并确保其名称与 findById 不同，避免歧义。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flight f WHERE f.flightNumber = :flightNumber")
    Optional<Flight> findByIdWithLock(@Param("flightNumber") String flightNumber);

    /**
     * 【新增】提供一个不加锁但预加载 airline 的 findById 方法，用于常规查询。
     * 这个方法名与 JpaRepository 的 findById 不同，避免了启动时的解析冲突。
     */
    @Query("SELECT f FROM Flight f JOIN FETCH f.airline WHERE f.flightNumber = :flightNumber")
    Optional<Flight> findByIdAndFetchAirline(@Param("flightNumber") String flightNumber);

}