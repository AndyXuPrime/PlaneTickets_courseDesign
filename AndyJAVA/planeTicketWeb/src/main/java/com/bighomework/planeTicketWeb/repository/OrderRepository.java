package com.bighomework.planeTicketWeb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bighomework.planeTicketWeb.entity.Order;
import com.bighomework.planeTicketWeb.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根据客户ID查询所有订单，并按创建时间降序排序。
     * 用于“我的订单”功能。
     * @param customerId 客户ID
     * @return 订单列表
     */
    List<Order> findByCustomerCustomerIdOrderByCreateTimeDesc(Integer customerId);

    /**
     * 根据客户ID和订单状态查询订单，并按创建时间降序排序。
     * 用于“我的订单”中按状态筛选。
     * @param customerId 客户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findByCustomerCustomerIdAndStatusOrderByCreateTimeDesc(Integer customerId, OrderStatus status);
}