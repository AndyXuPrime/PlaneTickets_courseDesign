package com.bighomework.planeTicketWeb.service;

import com.bighomework.planeTicketWeb.dto.requestDTO.BookingRequest;
import com.bighomework.planeTicketWeb.dto.responseDTO.LogicalOrderVO;
import com.bighomework.planeTicketWeb.dto.responseDTO.TicketInOrderVO;
import com.bighomework.planeTicketWeb.dto.responseDTO.TicketVO;
import com.bighomework.planeTicketWeb.entity.Customer;
import com.bighomework.planeTicketWeb.entity.Flight;
import com.bighomework.planeTicketWeb.entity.Ticket;
import com.bighomework.planeTicketWeb.enums.CabinClass;
import com.bighomework.planeTicketWeb.enums.MembershipLevel;
import com.bighomework.planeTicketWeb.enums.TicketStatus;
import com.bighomework.planeTicketWeb.exception.BusinessException;
import com.bighomework.planeTicketWeb.repository.CustomerRepository;
import com.bighomework.planeTicketWeb.repository.FlightRepository;
import com.bighomework.planeTicketWeb.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final FlightRepository flightRepository;
    private final CustomerRepository customerRepository;
    private final TicketRepository ticketRepository;
    // pricingStrategy 依赖保留，以防未来需要恢复复杂定价，但 createTickets 中不再使用它
    private final PricingStrategy pricingStrategy; 

    /**
     * 【核心实现】创建机票的业务逻辑，与前端简化逻辑同步
     */
    @Override
    @Transactional
    public List<TicketVO> createTickets(BookingRequest request, Integer bookerId) {
        logger.info("正在为预订人ID {} 创建航班 {} 的机票", bookerId, request.getFlightNumber());

        // 1. 检查并锁定航班，防止超卖
        Flight flight = flightRepository.findByIdWithLock(request.getFlightNumber())
                .orElseThrow(() -> new BusinessException("航班不存在: " + request.getFlightNumber()));
        
        Customer booker = customerRepository.findById(bookerId)
                .orElseThrow(() -> new BusinessException("预订人信息不存在: " + bookerId));
        
        Optional<Customer> passenger = customerRepository.findById(Integer.parseInt(request.getPassengerIds().get(0)));
        if (passenger.isEmpty()) {
            throw new BusinessException("部分或全部乘机人信息不存在");
        }

        // 2. 检查余票
//        int requiredSeats = passengers.;
        Short totalSeatsShort = (request.getCabinClass() == CabinClass.商务舱) 
            ? flight.getBusinessSeats() 
            : flight.getEconomySeats();
        int totalSeats = (totalSeatsShort != null) ? totalSeatsShort : 0;

        int soldSeats = ticketRepository.countSoldTickets(
            request.getFlightNumber(), request.getFlightDate(), request.getCabinClass(),
            List.of(TicketStatus.已预订, TicketStatus.已支付, TicketStatus.已使用)
        );
        if (totalSeats - soldSeats < 1) {
            throw new BusinessException("所选舱位余票不足");
        }

        List<Ticket> createdTickets = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now(); // 获取当前时间戳

//        for (Customer passenger : passengers) {
            Ticket ticket = new Ticket();
            
            // 3. 【核心修改】价格计算逻辑与前端保持一致
            // 直接使用 flight 的基础价格 (对应前端的 flight.price)
            BigDecimal basePrice = flight.getBasePrice();
            if (basePrice == null) {
                throw new BusinessException("航班 " + flight.getFlightNumber() + " 价格信息缺失，无法预订。");
            }
            
            // 在基础价格上应用会员折扣
            BigDecimal finalPrice = applyMembershipDiscount(basePrice, booker.getMembershipLevel());

            // 4. 填充 Ticket 实体所有字段
            ticket.setFlight(flight);
            ticket.setPassenger(passenger.get());
            ticket.setFlightDate(request.getFlightDate());
            ticket.setCabinClass(request.getCabinClass()); // 仍然记录用户选择的舱位
            ticket.setPrice(finalPrice); // 存入最终价格

            // 根据要求，直接设置为“已支付”状态，并记录时间和支付时间
            ticket.setStatus(TicketStatus.已支付);
            ticket.setBookingTime(now);
            ticket.setPaymentTime(now);
            
            createdTickets.add(ticket);
//        }

        // 5. 批量保存到数据库
        List<Ticket> savedTickets = ticketRepository.saveAll(createdTickets);
        logger.info("成功创建并支付了 {} 张机票。", savedTickets.size());
        
        return savedTickets.stream().map(this::convertToTicketVO).collect(Collectors.toList());
    }

    /**
     * 退票/取消预订
     */
    @Override
    @Transactional
    public void refundTicket(Long ticketId, String username) {
        logger.info("用户 {} 正在为机票ID {} 申请退票/取消", username, ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("机票不存在: " + ticketId));
        
        Customer operator = customerRepository.findByPhone(username)
                .orElseThrow(() -> new BusinessException("操作用户不存在"));

        if (!ticket.getPassenger().getCustomerId().equals(operator.getCustomerId())) {
             throw new BusinessException("您无权操作他人的机票");
        }
        
        if (ticket.getStatus() == TicketStatus.已取消 || ticket.getStatus() == TicketStatus.已使用) {
            throw new BusinessException("该机票状态为“" + ticket.getStatus().name() + "”，无法取消");
        }

        LocalDateTime departureDateTime = LocalDateTime.of(ticket.getFlightDate(), ticket.getFlight().getDepartureTime());
        if (LocalDateTime.now().isAfter(departureDateTime)) {
            throw new BusinessException("航班已起飞，无法取消");
        }
        
        ticket.setStatus(TicketStatus.已取消);
        ticket.setPaymentTime(null); // 满足数据库约束
        
        ticketRepository.save(ticket);
        logger.info("机票 {} 已成功取消。", ticketId);
    }

    /**
     * 获取我的订单列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<LogicalOrderVO> getMyOrders(String username) {
        Customer customer = customerRepository.findByPhone(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        List<Ticket> allTickets = ticketRepository.findByPassengerIdWithAssociations(customer.getCustomerId());

        Map<String, List<Ticket>> ticketsByBookingGroup = allTickets.stream()
            .collect(Collectors.groupingBy(t -> 
                t.getBookingTime().withNano(0).toString() + "@" + t.getFlight().getFlightNumber()
            ));

        List<LogicalOrderVO> logicalOrders = new ArrayList<>();
        for (Map.Entry<String, List<Ticket>> entry : ticketsByBookingGroup.entrySet()) {
            LogicalOrderVO logicalOrder = new LogicalOrderVO();
            List<Ticket> ticketsInOrder = entry.getValue();
            Ticket firstTicket = ticketsInOrder.get(0);

            logicalOrder.setLogicalOrderId(entry.getKey());
            logicalOrder.setFlightNumber(firstTicket.getFlight().getFlightNumber());
            logicalOrder.setDepartureAirport(firstTicket.getFlight().getDepartureAirport());
            logicalOrder.setArrivalAirport(firstTicket.getFlight().getArrivalAirport());
            logicalOrder.setFlightDate(firstTicket.getFlightDate());
            
            BigDecimal totalAmount = ticketsInOrder.stream()
                                                    .map(Ticket::getPrice)
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            logicalOrder.setTotalAmount(totalAmount);

            List<TicketInOrderVO> ticketVos = ticketsInOrder.stream()
                .map(this::convertToTicketInOrderVO)
                .collect(Collectors.toList());
            logicalOrder.setTickets(ticketVos);
            
            logicalOrders.add(logicalOrder);
        }
        
        return logicalOrders;
    }

    
    // --- 私有辅助方法 ---

    private TicketVO convertToTicketVO(Ticket ticket) {
        TicketVO vo = new TicketVO();
        vo.setTicketId(ticket.getTicketId());
        vo.setFlightNumber(ticket.getFlight().getFlightNumber());
        vo.setDepartureAirport(ticket.getFlight().getDepartureAirport());
        vo.setArrivalAirport(ticket.getFlight().getArrivalAirport());
        vo.setFlightDate(ticket.getFlightDate());
        vo.setPassengerName(ticket.getPassenger().getName());
        vo.setCabinClass(ticket.getCabinClass().name());
        vo.setFinalPrice(ticket.getPrice());
        vo.setStatus(ticket.getStatus().name());
        vo.setBookingTime(ticket.getBookingTime());
        return vo;
    }

    private TicketInOrderVO convertToTicketInOrderVO(Ticket ticket) {
        TicketInOrderVO vo = new TicketInOrderVO();
        vo.setTicketId(ticket.getTicketId());
        vo.setPassengerName(ticket.getPassenger().getName());
        vo.setCabinClass(ticket.getCabinClass().name());
        vo.setFinalPrice(ticket.getPrice());
        vo.setStatus(ticket.getStatus().name());
        return vo;
    }

    private BigDecimal applyMembershipDiscount(BigDecimal price, MembershipLevel level) {
        BigDecimal discountRate = switch (level) {
            case 白金 -> new BigDecimal("0.85");
            case 金卡 -> new BigDecimal("0.90");
            case 银卡 -> new BigDecimal("0.95");
            default -> BigDecimal.ONE;
        };
        return price.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

   
    
}