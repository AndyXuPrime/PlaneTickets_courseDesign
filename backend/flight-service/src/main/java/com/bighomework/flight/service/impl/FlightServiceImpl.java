package com.bighomework.flight.service.impl;

import com.bighomework.common.dto.responseDTO.FlightSearchVO;
import com.bighomework.common.enums.CabinClass;
import com.bighomework.common.enums.UserRole;
import com.bighomework.common.exception.BusinessException;
import com.bighomework.flight.entity.Flight;
import com.bighomework.flight.repository.FlightRepository;
import com.bighomework.flight.service.FlightService;
import com.bighomework.flight.service.PricingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final PricingStrategy pricingStrategy;

    @Override
    @Transactional(readOnly = true) // 解决关联对象懒加载问题
    @Cacheable(value = "flight_search", key = "#searchType + '-' + #value + '-' + #date")
    public List<FlightSearchVO> searchAvailableFlights(String searchType, String value, LocalDate date) {
        log.info("用户搜索航班: type={}, val={}, date={}", searchType, value, date);
        List<Flight> flights;
        try {
            if ("byRoute".equals(searchType)) {
                String[] parts = value.split("-");
                if (parts.length != 2) throw new BusinessException("航线格式错误");
                flights = flightRepository.findByRoute(parts[0], parts[1]);
            } else {
                flights = flightRepository.findByAirlineCode(value);
            }
            return flights.stream().map(f -> convertToVO(f, date)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索航班列表时发生崩溃: ", e);
            throw new BusinessException("系统繁忙，请稍后再试");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightSearchVO> findAllAvailableFlights(LocalDate date) {
        log.info("获取全量推荐航班列表，计算基准日期: {}", date);
        // 直接查询 flights 表所有数据
        return flightRepository.findAllWithAirline().stream()
                .map(f -> convertToVO(f, date)) // 使用传入的日期计算动态价格
                .collect(Collectors.toList());
    }

    /**
     * 核心转换逻辑：具备极强的容错能力
     */
    private FlightSearchVO convertToVO(Flight flight, LocalDate date) {
        FlightSearchVO vo = new FlightSearchVO();
        vo.setFlightNumber(flight.getFlightNumber());

        // --- 1. 航空公司信息保护 ---
        String airlineName = "未知航空公司";
        if (flight.getAirline() != null) {
            if (flight.getAirline().getAirlineName() != null) {
                airlineName = flight.getAirline().getAirlineName();
            } else {
                log.warn("数据警告：航班 {} 关联的航司对象存在但名称为空", flight.getFlightNumber());
            }
        } else {
            log.warn("数据严重警告：航班 {} 在数据库中缺失航司关联(airline_code可能无效)", flight.getFlightNumber());
        }
        vo.setAirlineName(airlineName);

        // --- 2. 基础字段保护 (防止 intValue() 抛出 NPE) ---
        BigDecimal basePrice = flight.getBasePrice() != null ? flight.getBasePrice() : new BigDecimal("500.00");
        int ecoTotal = (flight.getEconomySeats() != null) ? flight.getEconomySeats().intValue() : 0;
        int busTotal = (flight.getBusinessSeats() != null) ? flight.getBusinessSeats().intValue() : 0;

        if (flight.getBasePrice() == null) log.warn("数据修正：航班 {} 基础价为空，已采用默认值 500", flight.getFlightNumber());
        if (flight.getEconomySeats() == null) log.warn("数据修正：航班 {} 经济舱座位为空，已设为 0", flight.getFlightNumber());

        vo.setDepartureAirport(flight.getDepartureAirport() != null ? flight.getDepartureAirport() : "未知");
        vo.setArrivalAirport(flight.getArrivalAirport() != null ? flight.getArrivalAirport() : "未知");
        vo.setDepartureTime(flight.getDepartureTime());
        vo.setArrivalTime(flight.getArrivalTime());

        // --- 3. 动态定价与余票计算 ---
        try {
            // 假设已售为 0 (待后期对接 Order 服务)
            BigDecimal ecoPrice = pricingStrategy.calculatePrice(flight, date, CabinClass.经济舱, 0);
            BigDecimal busPrice = pricingStrategy.calculatePrice(flight, date, CabinClass.商务舱, 0);

            // 补充你要求的 VO 字段
            vo.setEconomyRemainingSeats(ecoTotal);
            vo.setBusinessRemainingSeats(busTotal);

            // 设置展示逻辑：优先显示有票的经济舱
            if (ecoTotal > 0) {
                vo.setPrice(ecoPrice);
                vo.setRemainingSeats(ecoTotal);
                vo.setCabinClassForDisplay("经济舱");
            } else if (busTotal > 0) {
                vo.setPrice(busPrice);
                vo.setRemainingSeats(busTotal);
                vo.setCabinClassForDisplay("商务舱");
            } else {
                vo.setPrice(ecoPrice);
                vo.setRemainingSeats(0);
                vo.setCabinClassForDisplay("无票");
            }
        } catch (Exception e) {
            log.error("逻辑错误：航班 {} 在计算价格时崩溃", flight.getFlightNumber(), e);
            vo.setPrice(basePrice);
            vo.setRemainingSeats(0);
            vo.setCabinClassForDisplay("计算异常");
        }

        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightSearchVO> findFlightByNumber(String flightNumber) {
        return flightRepository.findByFlightNumberLike(flightNumber).stream()
                .map(f -> convertToVO(f, LocalDate.now()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true) // 修改价格后，必须让所有搜索缓存失效
    public void updateFlightPrice(String flightNumber, BigDecimal newPrice, String role, String adminAirlineCode) {
        Flight flight = flightRepository.findById(flightNumber)
                .orElseThrow(() -> new BusinessException("航班不存在"));

        // 权限校验
        if (UserRole.ROLE_AIRLINE_ADMIN.name().equals(role)) {
            if (!flight.getAirline().getAirlineCode().equals(adminAirlineCode)) {
                throw new BusinessException("无权修改其他航司的票价");
            }
        }

        flight.setBasePrice(newPrice);
        flightRepository.save(flight);
        log.info("航班 {} 价格已更新为 {}", flightNumber, newPrice);
    }

    @Transactional(readOnly = true)
    @Override
    public FlightSearchVO findFlightByNumberAndDate(String flightNumber, LocalDate date) {
        Flight f = flightRepository.findById(flightNumber)
                .orElseThrow(() -> new BusinessException("航班不存在: " + flightNumber));
        return convertToVO(f, date);
    }

    @CacheEvict(value = "flight_search", allEntries = true)
    public void clearFlightCache() {
        log.info("管理端触发：已清理所有航班搜索缓存");
    }
}