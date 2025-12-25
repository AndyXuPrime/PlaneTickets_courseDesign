package com.bighomework.common.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalysisDTO {
    private String dimension; // 分析维度，如 '男', '女', '青年', '中年'
    private Long ticketCount; // 购票数量
    private Double averageSpent; // 人均消费
}