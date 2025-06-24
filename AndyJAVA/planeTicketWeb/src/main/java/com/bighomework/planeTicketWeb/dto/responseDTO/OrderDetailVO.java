package com.bighomework.planeTicketWeb.dto.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.bighomework.planeTicketWeb.enums.OrderStatus;

import lombok.Data;

@Data
public class OrderDetailVO {
    private Long orderId;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal totalAmount; // 代表实际支付金额
    private BigDecimal discountAmount; 
    private LocalDateTime createTime;
    private String bookerName; // 下单人
    private List<TicketVO> tickets;

    @Data
    public static class TicketVO {
        private Long ticketId;
        private String passengerName;
        private String passengerIdCard;
        private String cabinClass;
        private BigDecimal finalPrice;
        private String status;
    }
}