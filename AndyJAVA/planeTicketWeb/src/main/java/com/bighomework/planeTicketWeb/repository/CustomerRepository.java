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

    /**
     * 【核心新增】
     * 根据身份证号查找客户。这是修复注册功能BUG所必需的。
     * Spring Data JPA 会根据方法名自动生成此查询的实现。
     * @param idCard 身份证号
     * @return 包含客户信息的Optional对象
     */
    Optional<Customer> findByIdCard(String idCard);

    /**
     * 按性别分析客户购票规律。
     */
    @Query("SELECT new com.bighomework.planeTicketWeb.dto.stats.CustomerAnalysisDTO(" +
           "       c.gender, " +
           "       COUNT(t.id), " +
           "       AVG(t.finalPrice) " +
           ") " +
           "FROM Ticket t JOIN t.customer c " +
           "WHERE t.status IN (com.bighomework.planeTicketWeb.enums.TicketStatus.已支付, com.bighomework.planeTicketWeb.enums.TicketStatus.已使用) " +
           "GROUP BY c.gender")
    List<CustomerAnalysisDTO> analyzeByGender();

    /**
     * 按年龄段分析客户购票规律 (原生SQL)。
     */
    @Query(value = "SELECT " +
            "  CASE " +
            "    WHEN (YEAR(CURDATE()) - CAST(SUBSTRING(c.id_card, 7, 4) AS UNSIGNED)) < 18 THEN '少年 (0-17)' " +
            "    WHEN (YEAR(CURDATE()) - CAST(SUBSTRING(c.id_card, 7, 4) AS UNSIGNED)) BETWEEN 18 AND 40 THEN '青年 (18-40)' " +
            "    WHEN (YEAR(CURDATE()) - CAST(SUBSTRING(c.id_card, 7, 4) AS UNSIGNED)) BETWEEN 41 AND 60 THEN '中年 (41-60)' " +
            "    ELSE '老年 (60+)' " +
            "  END AS dimension, " +
            "  COUNT(t.ticket_id) AS ticketCount, " +
            "  AVG(t.final_price) AS averageSpent " +
            "FROM tickets t JOIN customers c ON t.customer_id = c.customer_id " +
            "WHERE t.status IN ('已支付', '已使用') " +
            "GROUP BY dimension " +
            "ORDER BY dimension",
            nativeQuery = true)
    List<Map<String, Object>> analyzeByAgeGroupNative();
}