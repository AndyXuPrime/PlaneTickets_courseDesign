package com.bighomework.planeTicketWeb.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bighomework.planeTicketWeb.dto.responseDTO.FlightSearchVO;
import com.bighomework.planeTicketWeb.entity.Flight;
import com.bighomework.planeTicketWeb.enums.CabinClass;
import com.bighomework.planeTicketWeb.enums.TicketStatus;
import com.bighomework.planeTicketWeb.exception.BusinessException;
import com.bighomework.planeTicketWeb.repository.FlightRepository;
import com.bighomework.planeTicketWeb.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
public class FlightServiceImpl implements FlightService {

    private static final Logger logger = LoggerFactory.getLogger(FlightServiceImpl.class);
    private final FlightRepository flightRepository;
    private final TicketRepository ticketRepository;
    private final PricingStrategy pricingStrategy;

    // ... 手动构造函数 ...
    public FlightServiceImpl(FlightRepository flightRepository,
                             TicketRepository ticketRepository,
                             PricingStrategy pricingStrategy) { // 确保参数里有它
        this.flightRepository = flightRepository;
        this.ticketRepository = ticketRepository;
        this.pricingStrategy = pricingStrategy; // <--- 必须有这一行！
    }

    @Override
    public List<FlightSearchVO> searchAvailableFlights(String searchType, String value, LocalDate date) {
        logger.info("Searching for flights with type '{}', value '{}' on date {}", searchType, value, date);

        List<Flight> flights;

        switch (searchType) {
            case "byRoute":
                String[] airports = value.split("-");
                if (airports.length != 2) {
                    throw new BusinessException("无效的航线格式，应为 '出发地-目的地'");
                }
                flights = flightRepository.findByDepartureAirportAndArrivalAirport(airports[0], airports[1]);
                break;

            case "byAirline":
                flights = flightRepository.findByAirlineAirlineCode(value);
                break;

            default:
                logger.warn("Unsupported search type: {}", searchType);
                return List.of();
        }

        if (flights.isEmpty()) {
            logger.warn("No flights found for the given criteria.");
            return List.of();
        }

        return flights.stream()
                .map(flight -> convertToFlightSearchVO(flight, date))
                .collect(Collectors.toList());
    }

    @Override
    public List<FlightSearchVO> findAllAvailableFlights(LocalDate date) {
        logger.info("Fetching all flights for date {}", date);
        List<Flight> flights = flightRepository.findAllWithAirline();

        if (flights.isEmpty()) {
            return List.of();
        }

        return flights.stream()
                .map(flight -> convertToFlightSearchVO(flight, date))
                .collect(Collectors.toList());
    }

    @Override
    public FlightSearchVO findFlightByNumberAndDate(String flightNumber, LocalDate date) {
        logger.info("Finding flight by number {} on date {}", flightNumber, date);

        Flight flight = flightRepository.findByIdAndFetchAirline(flightNumber)
                .orElseThrow(() -> new BusinessException("航班号 " + flightNumber + " 不存在"));

        return convertToFlightSearchVO(flight, date);
    }

    @Override
    // 【修改】返回类型为 List
    public List<FlightSearchVO> findFlightByNumber(String flightNumber) {
        logger.info("Finding flights matching number: {}", flightNumber);

        // 调用新的模糊查询方法
        List<Flight> flights = flightRepository.findByFlightNumberContaining(flightNumber);

        if (flights.isEmpty()) {
            throw new BusinessException("未找到包含 '" + flightNumber + "' 的航班");
        }

        LocalDate today = LocalDate.now();

        // 将所有匹配的航班转换为 VO
        return flights.stream()
                .map(flight -> convertToFlightSearchVO(flight, today))
                .collect(Collectors.toList());
    }

    private FlightSearchVO convertToFlightSearchVO(Flight flight, LocalDate date) {
        String airlineName = (flight.getAirline() != null) ? flight.getAirline().getAirlineName() : "未知航空公司";

        SeatInfo economySeatInfo = calculateSeatInfo(flight, date, CabinClass.经济舱);
        SeatInfo businessSeatInfo = calculateSeatInfo(flight, date, CabinClass.商务舱);

        FlightSearchVO vo = new FlightSearchVO();
        vo.setFlightNumber(flight.getFlightNumber());
        vo.setAirlineName(airlineName);
        vo.setDepartureAirport(flight.getDepartureAirport());
        vo.setArrivalAirport(flight.getArrivalAirport());
        vo.setDepartureTime(flight.getDepartureTime());
        vo.setArrivalTime(flight.getArrivalTime());


        if (economySeatInfo.getRemainingSeats() > 0) {
            vo.setPrice(economySeatInfo.getPrice());
            vo.setRemainingSeats(economySeatInfo.getRemainingSeats());
            vo.setCabinClassForDisplay(CabinClass.经济舱.name());
        }

        else if (businessSeatInfo.getRemainingSeats() > 0) {
            vo.setPrice(businessSeatInfo.getPrice());
            vo.setRemainingSeats(businessSeatInfo.getRemainingSeats());
            vo.setCabinClassForDisplay(CabinClass.商务舱.name());
        }

        else {
            vo.setPrice(economySeatInfo.getPrice());
            vo.setRemainingSeats(0);
            vo.setCabinClassForDisplay("无票");
        }
        return vo;
    }

    private SeatInfo calculateSeatInfo(Flight flight, LocalDate date, CabinClass cabinClass) {
        BigDecimal price = pricingStrategy.calculatePrice(flight, date, cabinClass);
        int soldSeats = ticketRepository.countSoldTickets(
                flight.getFlightNumber(), date, cabinClass,
                List.of(TicketStatus.已预订, TicketStatus.已支付, TicketStatus.已使用)
        );


        Short totalSeatsShort = (cabinClass == CabinClass.经济舱) ? flight.getEconomySeats() : flight.getBusinessSeats();
        int totalSeats = (totalSeatsShort != null) ? totalSeatsShort : 0;

        int remainingSeats = Math.max(0, totalSeats - soldSeats);
        return new SeatInfo(price, remainingSeats);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class SeatInfo {
        private BigDecimal price;
        private int remainingSeats;
    }
}