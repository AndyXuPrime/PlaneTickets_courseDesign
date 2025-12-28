package com.bighomework.flight.service.impl;

import com.bighomework.common.enums.CabinClass;
import com.bighomework.flight.entity.Flight;
import com.bighomework.flight.service.PricingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component("dynamicPricing")
public class DynamicPricingStrategyImpl implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Flight flight, LocalDate flightDate, CabinClass cabinClass, int soldSeats) {
        // 1. 基础价非空检查
        BigDecimal price = flight.getBasePrice();
        if (price == null) price = new BigDecimal("500.00");

        // 2. 舱位系数
        if (cabinClass == CabinClass.商务舱) {
            price = price.multiply(new BigDecimal("2.5"));
        }

        // 3. 时间系数 (防止 flightDate 为空)
        if (flightDate != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), flightDate);
            if (days <= 3) price = price.multiply(new BigDecimal("1.5"));
            else if (days <= 14) price = price.multiply(new BigDecimal("1.2"));
        }

        // 4. 上座率系数
        int total = (cabinClass == CabinClass.商务舱) ?
                (flight.getBusinessSeats() != null ? flight.getBusinessSeats() : 0) :
                (flight.getEconomySeats() != null ? flight.getEconomySeats() : 0);

        if (total > 0) {
            double rate = (double) soldSeats / total;
            if (rate > 0.9) price = price.multiply(new BigDecimal("1.4"));
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }
}