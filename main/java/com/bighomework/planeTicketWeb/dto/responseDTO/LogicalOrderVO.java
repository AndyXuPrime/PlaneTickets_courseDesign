package com.bighomework.planeTicketWeb.dto.responseDTO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class LogicalOrderVO {
    // 使用 flightNumber 和 flightDate 作为逻辑订单的唯一标识
    private String logicalOrderId; 
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDate flightDate;
    private BigDecimal totalAmount;
    private List<TicketInOrderVO> tickets;
}