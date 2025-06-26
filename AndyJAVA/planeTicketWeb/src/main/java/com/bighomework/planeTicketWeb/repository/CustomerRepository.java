package com.bighomework.planeTicketWeb.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bighomework.planeTicketWeb.dto.stats.CustomerAnalysisDTO;
import com.bighomework.planeTicketWeb.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByIdCard(String idCard);

    /**
     * 按性别分析客户购票规律。
     */
    @Query("SELECT new com.bighomework.planeTicketWeb.dto.stats.CustomerAnalysisDTO(" +
           "       c.gender, " +
           "       COUNT(t.id), " +
           "       AVG(t.price) " +
           ") " +
           // 【核心修复】将 t.customer c 修改为 t.passenger c，以匹配 Ticket 实体中的字段名
           "FROM Ticket t JOIN t.passenger c " + 
           "WHERE t.status IN (com.bighomework.planeTicketWeb.enums.TicketStatus.已支付, com.bighomework.planeTicketWeb.enums.TicketStatus.已使用) " +
           "GROUP BY c.gender")
    List<CustomerAnalysisDTO> analyzeByGender();

    /**
     * 按年龄段分析客户购票规律 (原生SQL)。
     * 注意：原生SQL查询的是数据库的物理列名，不受Java实体字段名重命名的影响。
     * tickets 表中的列名依然是 customer_id，所以这里的 ON t.customer_id = c.customer_id 是正确的，无需修改。
     */
    @Query(value = "SELECT " +
            "  CASE " +
            "    WHEN (YEAR(CURDATE()) - CAST(SUBSTRING(c.id_card, 7, 4) AS UNSIGNED)) < 18 THEN '少年 (0-17)' " +
            "    WHEN (YEAR(CURDATE()) - CAST(SUBSTRING(c.id_card, 7, 4) AS UNSIGNED)) BETWEEN 18 AND 40 THEN '青年 (18-40)' " +
            "    WHEN (YEAR(CURDATE()) - CAST(SUBSTRING(c.id_card, 7, 4) AS UNSIGNED)) BETWEEN 41 AND 60 THEN '中年 (41-60)' " +
            "    ELSE '老年 (60+)' " +
            "  END AS dimension, " +
            "  COUNT(t.ticket_id) AS ticketCount, " +
            "  AVG(t.price) AS averageSpent " +
            "FROM tickets t JOIN customers c ON t.customer_id = c.customer_id " +
            "WHERE t.status IN ('已支付', '已使用') " +
            "GROUP BY dimension " +
            "ORDER BY dimension",
            nativeQuery = true)
    List<Map<String, Object>> analyzeByAgeGroupNative();
}