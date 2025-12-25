package com.bighomework.flight.service;

import com.bighomework.common.enums.CabinClass;
import com.bighomework.flight.entity.Flight;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PricingStrategy {

    /**
     * 计算动态价格
     *
     * @param flight     航班实体（包含基础票价、总座位数）
     * @param flightDate 航班日期（用于计算时间因子）
     * @param cabinClass 舱位等级（经济/商务）
     * @param soldSeats  已售座位数（用于计算上座率因子，微服务中这个值由Service层通过Feign获取后传入）
     * @return 最终价格
     */
    BigDecimal calculatePrice(Flight flight, LocalDate flightDate, CabinClass cabinClass, int soldSeats);
}