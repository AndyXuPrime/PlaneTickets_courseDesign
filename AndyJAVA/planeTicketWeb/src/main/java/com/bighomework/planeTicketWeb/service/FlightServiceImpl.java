package com.bighomework.planeTicketWeb.service;

import com.bighomework.planeTicketWeb.dto.responseDTO.FlightSearchVO;
import com.bighomework.planeTicketWeb.entity.Flight;
import com.bighomework.planeTicketWeb.enums.CabinClass;
import com.bighomework.planeTicketWeb.enums.TicketStatus;
import com.bighomework.planeTicketWeb.exception.BusinessException;
import com.bighomework.planeTicketWeb.repository.FlightRepository;
import com.bighomework.planeTicketWeb.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private static final Logger logger = LoggerFactory.getLogger(FlightServiceImpl.class);

    private final FlightRepository flightRepository;
    private final TicketRepository ticketRepository;
    private final PricingStrategy pricingStrategy;

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
        // 【核心修改】调用 Repository 中新的、无歧义的方法名
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
        // 【核心修改】调用新的、无歧义的、能预加载 airline 的方法
        Flight flight = flightRepository.findByIdAndFetchAirline(flightNumber)
                .orElseThrow(() -> new BusinessException("航班号 " + flightNumber + " 不存在"));
        
        return convertToFlightSearchVO(flight, date);
    }

    @Override
    public FlightSearchVO findFlightByNumber(String flightNumber) {
        logger.info("Finding flight by number: {}", flightNumber);
        
        // 【核心修改】调用新的、无歧义的、能预加载 airline 的方法
        Flight flight = flightRepository.findByIdAndFetchAirline(flightNumber)
                .orElseThrow(() -> new BusinessException("航班号 " + flightNumber + " 不存在"));
        
        // 因为这个查询不依赖特定日期，我们在计算价格和余票时使用今天的日期作为默认值
        LocalDate today = LocalDate.now();
        
        return convertToFlightSearchVO(flight, today);
    }

    // --- 私有辅助方法 ---

    private FlightSearchVO convertToFlightSearchVO(Flight flight, LocalDate date) {
        // 检查 airline 是否为 null，防止在调用 getAirlineName() 时出现 NullPointerException
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

        // 优先显示经济舱信息，如果经济舱有票
        if (economySeatInfo.getRemainingSeats() > 0) {
            vo.setPrice(economySeatInfo.getPrice());
            vo.setRemainingSeats(economySeatInfo.getRemainingSeats());
            vo.setCabinClassForDisplay(CabinClass.经济舱.name());
        } 
        // 否则，如果商务舱有票，则显示商务舱信息
        else if (businessSeatInfo.getRemainingSeats() > 0) {
            vo.setPrice(businessSeatInfo.getPrice());
            vo.setRemainingSeats(businessSeatInfo.getRemainingSeats());
            vo.setCabinClassForDisplay(CabinClass.商务舱.name());
        } 
        // 如果都没有票，则显示经济舱的价格和0余票
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

        // 确保座位数不为null，避免 NullPointerException
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