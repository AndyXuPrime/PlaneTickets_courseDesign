package com.bighomework.admin.service.impl;

import com.bighomework.admin.service.AdminStatService;
import com.bighomework.common.dto.stats.GenericStatDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatServiceImpl implements AdminStatService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> analyzeCustomersByDimension(String dimension) {
        String sql = "";
        if ("gender".equalsIgnoreCase(dimension)) {
            sql = "SELECT u.gender as dimension, COUNT(t.ticket_id) as count FROM tickets t JOIN users u ON t.user_id = u.user_id GROUP BY u.gender";
        } else if ("age".equalsIgnoreCase(dimension)) {
            sql = "SELECT CASE " +
                    "WHEN (YEAR(CURDATE()) - CAST(SUBSTRING(u.id_card, 7, 4) AS UNSIGNED)) < 18 THEN '少年' " +
                    "WHEN (YEAR(CURDATE()) - CAST(SUBSTRING(u.id_card, 7, 4) AS UNSIGNED)) BETWEEN 18 AND 40 THEN '青年' " +
                    "ELSE '中老年' END as dimension, COUNT(t.ticket_id) as count " +
                    "FROM tickets t JOIN users u ON t.user_id = u.user_id GROUP BY dimension";
        }
        return Map.of("results", jdbcTemplate.queryForList(sql));
    }

    @Override
    public Map<String, Object> analyzeRoutes() {
        String sql = "SELECT flight_number, COUNT(*) as count FROM tickets GROUP BY flight_number";
        return Map.of("routes", jdbcTemplate.queryForList(sql));
    }

    @Override
    public Map<String, Object> getRouteSalesAnalysis(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT flight_number, SUM(price) as revenue FROM tickets WHERE booking_time BETWEEN ? AND ? GROUP BY flight_number";
        return Map.of("sales", jdbcTemplate.queryForList(sql, startDate, endDate));
    }

    @Override
    public List<GenericStatDTO> getStatsReport(String dimension) {
        // 简单实现
        return List.of();
    }
}