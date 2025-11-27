package com.bighomework.planeTicketWeb.dto.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResultVO {
    private Long orderId;
    private String orderNumber;
    private List<TicketVO> tickets;
}