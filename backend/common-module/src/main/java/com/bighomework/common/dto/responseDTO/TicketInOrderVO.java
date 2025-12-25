package com.bighomework.common.dto.responseDTO;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TicketInOrderVO {
    private Long ticketId;
    private String passengerName;
    private String cabinClass;
    private BigDecimal finalPrice;
    private String status;
}