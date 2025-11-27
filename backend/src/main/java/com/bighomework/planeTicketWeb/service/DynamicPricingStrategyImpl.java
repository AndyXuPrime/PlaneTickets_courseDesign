package com.bighomework.planeTicketWeb.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bighomework.planeTicketWeb.entity.Flight;
import com.bighomework.planeTicketWeb.enums.CabinClass;
import com.bighomework.planeTicketWeb.enums.TicketStatus;
import com.bighomework.planeTicketWeb.repository.TicketRepository;

@Component("dynamicPricing")
public class DynamicPricingStrategyImpl implements PricingStrategy {
    
    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public BigDecimal calculatePrice(Flight flight, LocalDate flightDate, CabinClass cabinClass) {
        // 【核心修复】检查 basePrice 是否为 null，并提供一个安全的默认值（例如 0）
        BigDecimal basePrice = Optional.ofNullable(flight.getBasePrice()).orElse(BigDecimal.ZERO);

        // 如果是商务舱，基础价格乘以系数
        if (cabinClass == CabinClass.商务舱) {
            basePrice = basePrice.multiply(new BigDecimal("2.5"));
        }

        // 根据起飞时间临近程度调整价格
        long daysUntilDeparture = ChronoUnit.DAYS.between(LocalDate.now(), flightDate);
        BigDecimal timeFactor = BigDecimal.ONE;
        if (daysUntilDeparture <= 3) {
            timeFactor = new BigDecimal("1.8");
        } else if (daysUntilDeparture <= 14) {
            timeFactor = new BigDecimal("1.3");
        }

        // 【核心修复】检查座位数是否为 null，并提供默认值 0
        Short totalSeatsShort = (cabinClass == CabinClass.商务舱) ? flight.getBusinessSeats() : flight.getEconomySeats();
        int totalSeats = Optional.ofNullable(totalSeatsShort).orElse((short) 0).intValue();

        // 如果总座位数为0，直接返回，避免除以零的错误
        if (totalSeats <= 0) {
            return basePrice.multiply(timeFactor).setScale(2, RoundingMode.HALF_UP);
        }

        // 根据上座率调整价格
        int soldSeats = ticketRepository.countSoldTickets(
            flight.getFlightNumber(), flightDate, cabinClass, 
            List.of(TicketStatus.已预订, TicketStatus.已支付, TicketStatus.已使用)
        );

        double occupancyRate = (double) soldSeats / totalSeats;
        BigDecimal occupancyFactor = BigDecimal.ONE;
        if (occupancyRate > 0.8) {
            occupancyFactor = new BigDecimal("1.5");
        } else if (occupancyRate > 0.6) {
            occupancyFactor = new BigDecimal("1.2");
        }

        // 计算最终价格
        return basePrice.multiply(timeFactor).multiply(occupancyFactor)
                        .setScale(2, RoundingMode.HALF_UP);
    }
}