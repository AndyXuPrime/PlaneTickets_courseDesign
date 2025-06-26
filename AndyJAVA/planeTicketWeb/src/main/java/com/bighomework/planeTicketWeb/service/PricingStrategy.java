package com.bighomework.planeTicketWeb.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.bighomework.planeTicketWeb.entity.Flight;
import com.bighomework.planeTicketWeb.enums.CabinClass;


public interface PricingStrategy {

    /**
     * 根据航班信息、日期和舱位计算价格。
     *
     * @param flight     航班对象，包含基础价格、座位数等信息。
     * @param flightDate 航班日期，用于判断淡旺季或临近起飞时间。
     * @param cabinClass 舱位等级（经济舱、商务舱等）。
     * @return 计算出的最终动态价格。
     */
    BigDecimal calculatePrice(Flight flight, LocalDate flightDate, CabinClass cabinClass);

}
