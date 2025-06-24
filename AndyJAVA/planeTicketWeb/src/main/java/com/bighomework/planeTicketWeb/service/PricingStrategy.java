package com.bighomework.planeTicketWeb.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.bighomework.planeTicketWeb.entity.Flight;
import com.bighomework.planeTicketWeb.enums.CabinClass;

/**
 * 定价策略接口。
 * <p>
 * 定义了计算机票价格的统一方法，允许系统灵活地切换不同的定价算法。
 * 任何定价策略的实现类都必须实现这个接口。
 */
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
