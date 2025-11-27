package com.bighomework.planeTicketWeb.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenericStatDTO {
    private String dimension; // 维度 (如航空公司名, 机型)
    private Long count;       // 计数值
}