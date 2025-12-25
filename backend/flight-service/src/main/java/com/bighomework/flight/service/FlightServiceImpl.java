package com.bighomework.flight.service;

import com.bighomework.common.dto.responseDTO.FlightSearchVO;
import com.bighomework.common.enums.CabinClass;
import com.bighomework.common.exception.BusinessException;
import com.bighomework.flight.entity.Flight;
import com.bighomework.flight.repository.FlightRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    // private final TicketRepository ticketRepository; // 删除！微服务不能直接查别的库
    private final PricingStrategy pricingStrategy;

    // 注入 Feign 客户端 (如果还没写 Order 服务，可以先暂时把调用 feign 的地方写死返回 0)
    // private final OrderFeignClient orderFeignClient;

    @Override
    // 【新增】Redis 缓存：查询结果缓存 10 分钟 (需要在配置文件开启缓存)
    // key 生成规则：searchType-value-date
    @Cacheable(value = "flight_search", key = "#searchType + '-' + #value + '-' + #date")
    public List<FlightSearchVO> searchAvailableFlights(String searchType, String value, LocalDate date) {
        log.info("Searching flights: type={}, value={}, date={}", searchType, value, date);

        List<Flight> flights;
        switch (searchType) {
            case "byRoute":
                String[] airports = value.split("-");
                if (airports.length != 2) throw new BusinessException("格式错误");
                flights = flightRepository.findByDepartureAirportAndArrivalAirport(airports[0], airports[1]);
                break;
            case "byAirline":
                flights = flightRepository.findByAirlineAirlineCode(value);
                break;
            default:
                return List.of();
        }

        return flights.stream()
                .map(flight -> convertToFlightSearchVO(flight, date))
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightSearchVO> findAllAvailableFlights(LocalDate date) {
        return flightRepository.findAllWithAirline().stream()
                .map(f -> convertToFlightSearchVO(f, date))
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightSearchVO> findFlightByNumber(String flightNumber) {
        List<Flight> flights = flightRepository.findByFlightNumberContaining(flightNumber);
        LocalDate today = LocalDate.now();
        return flights.stream()
                .map(f -> convertToFlightSearchVO(f, today))
                .collect(Collectors.toList());
    }

    @Override
    public FlightSearchVO findFlightByNumberAndDate(String flightNumber, LocalDate date) {
        Flight flight = flightRepository.findByIdAndFetchAirline(flightNumber)
                .orElseThrow(() -> new BusinessException("航班不存在"));
        return convertToFlightSearchVO(flight, date);
    }

    // --- 转换逻辑 ---

    private FlightSearchVO convertToFlightSearchVO(Flight flight, LocalDate date) {
        String airlineName = (flight.getAirline() != null) ? flight.getAirline().getAirlineName() : "未知";

        // 计算座位信息
        SeatInfo economy = calculateSeatInfo(flight, date, CabinClass.经济舱);
        SeatInfo business = calculateSeatInfo(flight, date, CabinClass.商务舱);

        FlightSearchVO vo = new FlightSearchVO();
        vo.setFlightNumber(flight.getFlightNumber());
        vo.setAirlineName(airlineName);
        vo.setDepartureAirport(flight.getDepartureAirport());
        vo.setArrivalAirport(flight.getArrivalAirport());
        vo.setDepartureTime(flight.getDepartureTime());
        vo.setArrivalTime(flight.getArrivalTime());

        // 简单的展示逻辑：优先展示经济舱，如果没票展示商务舱
        if (economy.remainingSeats > 0) {
            vo.setPrice(economy.price);
            vo.setRemainingSeats(economy.remainingSeats);
            vo.setCabinClassForDisplay(CabinClass.经济舱.name());
        } else if (business.remainingSeats > 0) {
            vo.setPrice(business.price);
            vo.setRemainingSeats(business.remainingSeats);
            vo.setCabinClassForDisplay(CabinClass.商务舱.name());
        } else {
            vo.setPrice(economy.price);
            vo.setRemainingSeats(0);
            vo.setCabinClassForDisplay("无票");
        }
        return vo;
    }

    private SeatInfo calculateSeatInfo(Flight flight, LocalDate date, CabinClass cabinClass) {
        // 1. 【第一步】先获取已售座位数
        // ---------------------------------------------------------
        // 方案1：调用 Order 服务 (标准微服务做法 - 待实现)
        // ApiResponse<Integer> resp = orderFeignClient.getSoldSeats(flight.getFlightNumber(), date, cabinClass);
        // int soldSeats = (resp != null && resp.getData() != null) ? resp.getData() : 0;

        // 方案2：(临时方案，为了让你能先跑通) 暂时假设卖了 0 张
        int soldSeats = 0;
        // ---------------------------------------------------------

        // 2. 【第二步】调用策略计算价格 (现在需要传入 soldSeats 了)
        BigDecimal price = pricingStrategy.calculatePrice(flight, date, cabinClass, soldSeats);

        // 3. 【第三步】计算剩余座位
        Short totalShort = (cabinClass == CabinClass.经济舱) ? flight.getEconomySeats() : flight.getBusinessSeats();
        int totalSeats = (totalShort != null) ? totalShort : 0;

        int remainingSeats = Math.max(0, totalSeats - soldSeats);

        return new SeatInfo(price, remainingSeats);
    }

    @Data
    @lombok.AllArgsConstructor
    private static class SeatInfo {
        BigDecimal price;
        int remainingSeats;
    }
}