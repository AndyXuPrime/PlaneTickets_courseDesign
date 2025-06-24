// --- START OF FILE OrderServiceImpl.txt ---

package com.bighomework.planeTicketWeb.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bighomework.planeTicketWeb.dto.requestDTO.OrderRequest;
import com.bighomework.planeTicketWeb.dto.responseDTO.OrderDetailVO;
import com.bighomework.planeTicketWeb.entity.Customer;
import com.bighomework.planeTicketWeb.entity.Flight;
import com.bighomework.planeTicketWeb.entity.Order;
import com.bighomework.planeTicketWeb.entity.Ticket;
import com.bighomework.planeTicketWeb.enums.CabinClass;
import com.bighomework.planeTicketWeb.enums.MembershipLevel;
import com.bighomework.planeTicketWeb.enums.OrderStatus;
import com.bighomework.planeTicketWeb.enums.TicketStatus;
import com.bighomework.planeTicketWeb.exception.BusinessException;
import com.bighomework.planeTicketWeb.repository.CustomerRepository;
import com.bighomework.planeTicketWeb.repository.FlightRepository;
import com.bighomework.planeTicketWeb.repository.OrderRepository;
import com.bighomework.planeTicketWeb.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final FlightRepository flightRepository;
    private final CustomerRepository customerRepository;
    private final TicketRepository ticketRepository;
    private final OrderRepository orderRepository;
    private final PricingStrategy pricingStrategy;

    @Override
    @Transactional
    public OrderDetailVO createOrder(OrderRequest request, Integer bookerId) {
        // ... 此处代码保持不变 ...
        logger.info("Creating order for flight {} on {} by customer {}", request.getFlightNumber(), request.getFlightDate(), bookerId);
        
        Flight flight = flightRepository.findByIdWithLock(request.getFlightNumber())
                .orElseThrow(() -> new BusinessException("航班不存在"));
        
        Customer booker = customerRepository.findById(bookerId)
                .orElseThrow(() -> new BusinessException("下单用户不存在"));
        
        List<Customer> passengers = customerRepository.findAllById(request.getPassengerIds());
        if (passengers.size() != request.getPassengerIds().size()) {
            throw new BusinessException("部分乘机人信息不存在");
        }

        int totalSeats = request.getCabinClass() == CabinClass.商务舱 ? flight.getBusinessSeats() : flight.getEconomySeats();
        int soldSeats = ticketRepository.countSoldTickets(
            request.getFlightNumber(), request.getFlightDate(), request.getCabinClass(),
            List.of(TicketStatus.已预订, TicketStatus.已支付, TicketStatus.已使用)
        );
        if (totalSeats - soldSeats < passengers.size()) {
            throw new BusinessException("该舱位余票不足");
        }

        Order order = new Order();
        order.setCustomer(booker);
        order.setOrderNumber("ORD" + System.currentTimeMillis());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCreateTime(LocalDateTime.now());

        BigDecimal totalOriginalPrice = BigDecimal.ZERO;
        BigDecimal totalFinalPrice = BigDecimal.ZERO;
        List<Ticket> newTickets = new ArrayList<>();
        for (Customer passenger : passengers) {
            Ticket ticket = new Ticket();
            BigDecimal originalPrice = pricingStrategy.calculatePrice(flight, request.getFlightDate(), request.getCabinClass());
            
            BigDecimal finalPrice = applyMembershipDiscount(originalPrice, booker.getMembershipLevel());

            ticket.setOrder(order);
            ticket.setFlight(flight);
            ticket.setCustomer(passenger);
            ticket.setFlightDate(request.getFlightDate());
            ticket.setCabinClass(request.getCabinClass());
            ticket.setPrice(originalPrice);
            ticket.setFinalPrice(finalPrice);
            ticket.setStatus(TicketStatus.已预订);
            ticket.setBookingTime(LocalDateTime.now());
            
            newTickets.add(ticket);
            totalOriginalPrice = totalOriginalPrice.add(originalPrice);
            totalFinalPrice = totalFinalPrice.add(finalPrice);
        }
        order.setTickets(newTickets);
        order.setTotalAmount(totalOriginalPrice);
        order.setActualAmount(totalFinalPrice);
        order.setDiscountAmount(totalOriginalPrice.subtract(totalFinalPrice));

        Order savedOrder = orderRepository.save(order);
        logger.info("Order {} created successfully.", savedOrder.getOrderNumber());
        return convertToDetailVO(savedOrder);
    }
    
    @Override
    @Transactional
    public void refundTicket(Long ticketId, String operator) {
        // ... 此处代码保持不变 ...
        logger.info("Processing refund for ticket {} by {}", ticketId, operator);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("机票 " + ticketId + " 不存在"));

        if (ticket.getStatus() != TicketStatus.已支付) {
            throw new BusinessException("只有已支付的机票才能退票，当前状态: " + ticket.getStatus());
        }

        Flight flight = ticket.getFlight();
        if (flight == null) {
            throw new BusinessException("无法获取机票关联的航班信息");
        }
        LocalDateTime departureDateTime = LocalDateTime.of(ticket.getFlightDate(), flight.getDepartureTime());

        if (LocalDateTime.now().isAfter(departureDateTime)) {
            throw new BusinessException("航班已起飞，无法退票");
        }

        BigDecimal refundFee = calculateRefundFee(ticket.getFinalPrice(), departureDateTime);
        BigDecimal refundAmount = ticket.getFinalPrice().subtract(refundFee);

        ticket.setStatus(TicketStatus.已取消);
        ticketRepository.save(ticket);
        
        updateOrderStatusAfterAction(ticket.getOrder());

        logger.info("Refunding ticket {}. Final price: {}, Refund fee: {}, Refund amount: {}",
                ticketId, ticket.getFinalPrice(), refundFee, refundAmount);
    }

    @Override
    public OrderDetailVO findOrderDetailsById(Long orderId) {
        // ... 此处代码保持不变 ...
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单 " + orderId + " 不存在"));
        return convertToDetailVO(order);
    }

    /**
     * 【新增方法的实现】
     */
    @Override
    public List<OrderDetailVO> findOrdersByUsername(String username) {
        logger.info("正在为用户 (手机号: {}) 查询订单列表", username);

        // 1. 根据用户名（手机号）找到 Customer 实体
        Customer customer = customerRepository.findByPhone(username)
                .orElseThrow(() -> new BusinessException("用户不存在或凭证无效"));

        // 2. 使用 OrderRepository 中已有的方法，根据 customerId 查询所有订单
        //    该方法已按创建时间降序排序，非常适合“我的订单”页面的展示
        List<Order> orders = orderRepository.findByCustomerCustomerIdOrderByCreateTimeDesc(customer.getCustomerId());

        // 3. 将 Order 实体列表转换为 OrderDetailVO 列表并返回
        //    这里我们复用已有的 convertToDetailVO 辅助方法
        return orders.stream()
                     .map(this::convertToDetailVO)
                     .collect(Collectors.toList());
    }

    // --- 私有辅助方法 ---

    private BigDecimal applyMembershipDiscount(BigDecimal price, MembershipLevel level) {
        // ... 此处代码保持不变 ...
        BigDecimal discountRate = switch (level) {
            case 白金 -> new BigDecimal("0.85");
            case 金卡 -> new BigDecimal("0.90");
            case 银卡 -> new BigDecimal("0.95");
            case 普通 -> BigDecimal.ONE;
        };
        return price.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRefundFee(BigDecimal price, LocalDateTime departureDateTime) {
        // ... 此处代码保持不变 ...
        long hoursUntilDeparture = ChronoUnit.HOURS.between(LocalDateTime.now(), departureDateTime);
        
        if (hoursUntilDeparture > 72) {
            return price.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        } else if (hoursUntilDeparture > 24) {
            return price.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        } else {
            return price.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    private void updateOrderStatusAfterAction(Order order) {
        // ... 此处代码保持不变 ...
        Order freshOrder = orderRepository.findById(order.getOrderId()).orElse(order);
        
        boolean allTicketsCancelled = freshOrder.getTickets().stream()
                .allMatch(t -> t.getStatus() == TicketStatus.已取消);

        if (allTicketsCancelled) {
            freshOrder.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(freshOrder);
            logger.info("All tickets in order {} are cancelled. Order status updated to CANCELLED.", freshOrder.getOrderNumber());
        }
    }

    private OrderDetailVO convertToDetailVO(Order order) {
        // ... 此处代码保持不变 ...
        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNumber(order.getOrderNumber());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getActualAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setCreateTime(order.getCreateTime());
        
        if (order.getCustomer() != null) {
            vo.setBookerName(order.getCustomer().getName());
        }

        List<OrderDetailVO.TicketVO> ticketVOs = order.getTickets().stream().map(ticket -> {
            OrderDetailVO.TicketVO ticketVO = new OrderDetailVO.TicketVO();
            ticketVO.setTicketId(ticket.getTicketId());
            if (ticket.getCustomer() != null) {
                ticketVO.setPassengerName(ticket.getCustomer().getName());
                ticketVO.setPassengerIdCard(ticket.getCustomer().getIdCard());
            }
            ticketVO.setCabinClass(ticket.getCabinClass().name());
            ticketVO.setFinalPrice(ticket.getFinalPrice());
            ticketVO.setStatus(ticket.getStatus().name());
            return ticketVO;
        }).collect(Collectors.toList());

        vo.setTickets(ticketVOs);
        return vo;
    }

     @Override
    @Transactional
    public void cancelOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException("订单不存在"));

        // 权限校验：确保是订单本人操作
        if (!order.getCustomer().getPhone().equals(username)) {
            throw new BusinessException("无权操作此订单");
        }
        
        // 业务逻辑校验：只有待支付的订单可以取消
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("此订单状态无法取消");
        }

        order.setStatus(OrderStatus.CANCELLED);
        // 还需要将机票库存还回去等逻辑...
        orderRepository.save(order);
    }
}