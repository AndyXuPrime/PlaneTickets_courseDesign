package com.bighomework.flight.service;

import com.bighomework.common.enums.CabinClass;
import com.bighomework.flight.entity.Flight;
import com.bighomework.flight.service.PricingStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component("dynamicPricing")
public class DynamicPricingStrategyImpl implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Flight flight, LocalDate flightDate, CabinClass cabinClass, int soldSeats) {
        // 1. 获取基础票价
        BigDecimal basePrice = Optional.ofNullable(flight.getBasePrice()).orElse(BigDecimal.ZERO);

        // 2. 舱位系数
        if (cabinClass == CabinClass.商务舱) {
            basePrice = basePrice.multiply(new BigDecimal("2.5"));
        }

        // 3. 时间系数 (越临近越贵)
        long daysUntilDeparture = ChronoUnit.DAYS.between(LocalDate.now(), flightDate);
        BigDecimal timeFactor = BigDecimal.ONE;
        if (daysUntilDeparture <= 3) {
            timeFactor = new BigDecimal("1.5"); // 临近3天涨价50%
        } else if (daysUntilDeparture <= 14) {
            timeFactor = new BigDecimal("1.2"); // 临近2周涨价20%
        }

        // 4. 上座率系数 (人越多越贵)
        // 获取该舱位总座位数
        Short totalSeatsShort = (cabinClass == CabinClass.商务舱) ? flight.getBusinessSeats() : flight.getEconomySeats();
        int totalSeats = Optional.ofNullable(totalSeatsShort).orElse((short) 0).intValue();

        BigDecimal occupancyFactor = BigDecimal.ONE;
        if (totalSeats > 0) {
            double occupancyRate = (double) soldSeats / totalSeats;
            if (occupancyRate > 0.9) {
                occupancyFactor = new BigDecimal("1.4"); // 剩最后10%座位，涨价40%
            } else if (occupancyRate > 0.7) {
                occupancyFactor = new BigDecimal("1.1"); // 剩30%座位，涨价10%
            }
        }

        // 5. 计算最终价格 = 基础价 * 时间系数 * 上座率系数
        return basePrice.multiply(timeFactor)
                .multiply(occupancyFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }
}