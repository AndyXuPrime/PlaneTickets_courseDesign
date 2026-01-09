package com.bighomework.flight.service.impl;

import com.bighomework.common.dto.requestDTO.FlightRequest;
import com.bighomework.common.dto.responseDTO.FlightSearchVO;
import com.bighomework.common.enums.CabinClass;
import com.bighomework.common.enums.UserRole;
import com.bighomework.common.exception.BusinessException;
import com.bighomework.flight.entity.Airline;
import com.bighomework.flight.entity.Flight;
import com.bighomework.flight.repository.AirlineRepository;
import com.bighomework.flight.repository.FlightRepository;
import com.bighomework.flight.service.FlightService;
import com.bighomework.flight.service.PricingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
    private final AirlineRepository airlineRepository;
    private final PricingStrategy pricingStrategy;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "flight_search", key = "#searchType + '-' + #value + '-' + #date")
    public List<FlightSearchVO> searchAvailableFlights(String searchType, String value, LocalDate date) {
        List<Flight> flights;
        try {
            if ("byRoute".equals(searchType)) {
                String[] parts = value.split("-");
                if (parts.length != 2) throw new BusinessException("航线参数非法");
                flights = flightRepository.findByRoute(parts[0], parts[1]);
            } else {
                flights = flightRepository.findByAirlineCode(value);
            }
            return flights.stream().map(f -> convertToVO(f, date)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询航班列表异常: ", e);
            throw new BusinessException("查询失败，请稍后再试");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightSearchVO> findAllAvailableFlights(LocalDate date) {
        return flightRepository.findAllWithAirline().stream()
                .map(f -> convertToVO(f, date))
                .collect(Collectors.toList());
    }

    private FlightSearchVO convertToVO(Flight flight, LocalDate date) {
        FlightSearchVO vo = new FlightSearchVO();
        vo.setFlightNumber(flight.getFlightNumber());

        String airlineName = (flight.getAirline() != null && flight.getAirline().getAirlineName() != null)
                ? flight.getAirline().getAirlineName() : "未知航司";
        vo.setAirlineName(airlineName);

        BigDecimal basePrice = flight.getBasePrice() != null ? flight.getBasePrice() : new BigDecimal("500.00");
        int ecoTotal = flight.getEconomySeats() != null ? flight.getEconomySeats().intValue() : 0;
        int busTotal = flight.getBusinessSeats() != null ? flight.getBusinessSeats().intValue() : 0;

        vo.setDepartureAirport(flight.getDepartureAirport());
        vo.setArrivalAirport(flight.getArrivalAirport());
        vo.setDepartureTime(flight.getDepartureTime());
        vo.setArrivalTime(flight.getArrivalTime());

        try {
            BigDecimal ecoPrice = pricingStrategy.calculatePrice(flight, date, CabinClass.经济舱, 0);
            BigDecimal busPrice = pricingStrategy.calculatePrice(flight, date, CabinClass.商务舱, 0);

            vo.setEconomyRemainingSeats(ecoTotal);
            vo.setBusinessRemainingSeats(busTotal);

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
            log.error("计算票价异常 [{}]: {}", flight.getFlightNumber(), e.getMessage());
            vo.setPrice(basePrice);
            vo.setRemainingSeats(0);
            vo.setCabinClassForDisplay("定价异常");
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
    @CacheEvict(value = "flight_search", allEntries = true)
    public void updateFlightPrice(String flightNumber, BigDecimal newPrice, String role, String adminAirlineCode) {
        Flight flight = flightRepository.findById(flightNumber).orElseThrow(() -> new BusinessException("航班不存在"));

        if (UserRole.ROLE_AIRLINE_ADMIN.name().equals(role)) {
            if (!flight.getAirline().getAirlineCode().equals(adminAirlineCode)) {
                throw new BusinessException("无权修改非本航司票价");
            }
        }

        flight.setBasePrice(newPrice);
        flightRepository.save(flight);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightSearchVO findFlightByNumberAndDate(String flightNumber, LocalDate date) {
        Flight f = flightRepository.findById(flightNumber).orElseThrow(() -> new BusinessException("航班不存在"));
        return convertToVO(f, date);
    }

    @Override
    @CacheEvict(value = "flight_search", allEntries = true)
    public void clearFlightCache() {
        log.info("已清理航班搜索缓存");
    }

    @Override
    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true)
    public void saveFlight(FlightRequest req) {
        if (flightRepository.existsById(req.getFlightNumber())) {
            throw new BusinessException("航班号已存在");
        }

        Flight flight = new Flight();
        BeanUtils.copyProperties(req, flight);

        Airline airline = airlineRepository.findById(req.getAirlineCode())
                .orElseThrow(() -> new BusinessException("航空公司不存在"));
        flight.setAirline(airline);

        flightRepository.save(flight);
    }

    @Override
    @Transactional
    @CacheEvict(value = "flight_search", allEntries = true)
    public void addAirline(Airline airline) {
        if (airlineRepository.existsById(airline.getAirlineCode())) {
            throw new BusinessException("该航司二字码 (" + airline.getAirlineCode() + ") 已存在");
        }

        if (airline.getAirlineName() == null || airline.getAirlineName().isEmpty()) {
            throw new BusinessException("航司名称不能为空");
        }

        airlineRepository.save(airline);
        log.info("新增航空公司成功: {} - {}", airline.getAirlineCode(), airline.getAirlineName());
    }
}