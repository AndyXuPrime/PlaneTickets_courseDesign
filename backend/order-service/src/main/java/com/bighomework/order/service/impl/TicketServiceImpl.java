package com.bighomework.order.service.impl;

import com.bighomework.common.dto.requestDTO.BookingRequest;
import com.bighomework.common.dto.responseDTO.*;
import com.bighomework.common.enums.CabinClass;
import com.bighomework.common.enums.MembershipLevel;
import com.bighomework.common.enums.TicketStatus;
import com.bighomework.common.exception.BusinessException;
import com.bighomework.common.feign.AuthFeignClient;
import com.bighomework.common.feign.FlightFeignClient;
import com.bighomework.common.util.ApiResponse;
import com.bighomework.order.entity.Ticket;
import com.bighomework.order.repository.TicketRepository;
import com.bighomework.order.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final FlightFeignClient flightFeignClient;
    private final AuthFeignClient authFeignClient;

    @Override
    @Transactional
    public List<TicketVO> createTickets(BookingRequest request, String username) {
        log.info("用户 {} 请求预订航班 {}", username, request.getFlightNumber());

        // 1. 远程调用 Auth 服务获取用户信息 (获取 userId 和会员等级)
        ApiResponse<LoginResponse> userResp = authFeignClient.getUserByPhone(username);
        if (userResp.getData() == null) {
            throw new BusinessException("用户不存在");
        }
        LoginResponse booker = userResp.getData();

        // 2. 远程调用 Flight 服务获取航班信息 (获取基础价格、座位数)
        ApiResponse<FlightSearchVO> flightResp = flightFeignClient.getFlightByNumber(request.getFlightNumber());
        if (flightResp.getData() == null) {
            throw new BusinessException("航班不存在: " + request.getFlightNumber());
        }
        FlightSearchVO flight = flightResp.getData();

        // 3. 检查库存 (简单版：查本地订单数。进阶版：调用 Flight 服务查 DailyStock)
        // 这里演示简单版，结合 Flight 信息里的 totalSeats
        int soldSeats = ticketRepository.countSoldTickets(
                request.getFlightNumber(), request.getFlightDate(), request.getCabinClass(),
                List.of(TicketStatus.已预订, TicketStatus.已支付, TicketStatus.已使用)
        );

        // 注意：FlightSearchVO 需要包含 totalSeats 信息，或者你需要修改 FlightFeignClient 返回更详细的 DTO
        // 这里假设 flight.getRemainingSeats() 是准确的，或者你暂时略过严格校验
        if (flight.getRemainingSeats() < request.getPassengerIds().size()) {
            throw new BusinessException("余票不足");
        }

        // 4. 创建订单
        List<Ticket> createdTickets = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 计算价格 (这里简化处理，直接用 Flight 服务返回的当前价格)
        BigDecimal currentPrice = flight.getPrice();
        BigDecimal finalPrice = applyMembershipDiscount(currentPrice, booker.getMembershipLevel());

        for (String passengerIdStr : request.getPassengerIds()) {
            Ticket ticket = new Ticket();
            ticket.setFlightNumber(request.getFlightNumber());
            ticket.setFlightDate(request.getFlightDate());
            ticket.setUserId(booker.getId()); // 这里简化：假设乘机人就是预订人，或者你需要遍历 passengerIds
            ticket.setCabinClass(request.getCabinClass());
            ticket.setPrice(finalPrice);
            ticket.setStatus(TicketStatus.已支付);
            ticket.setBookingTime(now);
            ticket.setPaymentTime(now);
            createdTickets.add(ticket);
        }

        List<Ticket> savedTickets = ticketRepository.saveAll(createdTickets);

        // 5. (可选) 远程调用 Flight 服务扣减库存 (Order -> Flight)
        // flightFeignClient.reduceStock(...)

        return savedTickets.stream().map(t -> convertToTicketVO(t, flight, booker.getName())).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void refundTicket(Long ticketId, String username) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("机票不存在"));

        // 远程查用户ID
        ApiResponse<LoginResponse> userResp = authFeignClient.getUserByPhone(username);
        Integer currentUserId = userResp.getData().getId();

        if (!ticket.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权操作他人订单");
        }

        if (ticket.getStatus() == TicketStatus.已取消) {
            throw new BusinessException("重复取消");
        }

        // 简单校验时间
        // LocalDateTime departure = LocalDateTime.of(ticket.getFlightDate(), LocalTime.MIN); // 需要从 Flight 服务查具体时间
        // if (LocalDateTime.now().isAfter(departure)) ...

        ticket.setStatus(TicketStatus.已取消);
        ticketRepository.save(ticket);
    }

    @Override
    public List<LogicalOrderVO> getMyOrders(String username) {
        ApiResponse<LoginResponse> userResp = authFeignClient.getUserByPhone(username);
        Integer userId = userResp.getData().getId();

        List<Ticket> tickets = ticketRepository.findByUserId(userId);

        // 分组逻辑
        Map<String, List<Ticket>> grouped = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getBookingTime().toString() + "@" + t.getFlightNumber()));

        List<LogicalOrderVO> result = new ArrayList<>();
        for (List<Ticket> group : grouped.values()) {
            Ticket first = group.get(0);

            // 需要远程调用 Flight 服务补充 机场信息
            // 这是一个 N+1 问题，实际生产中会用缓存优化，或者在 Ticket 表冗余机场代码
            ApiResponse<FlightSearchVO> fResp = flightFeignClient.getFlightByNumber(first.getFlightNumber());
            String dep = fResp.getData() != null ? fResp.getData().getDepartureAirport() : "Unknown";
            String arr = fResp.getData() != null ? fResp.getData().getArrivalAirport() : "Unknown";

            LogicalOrderVO vo = new LogicalOrderVO();
            vo.setLogicalOrderId(first.getBookingTime().toString());
            vo.setFlightNumber(first.getFlightNumber());
            vo.setDepartureAirport(dep);
            vo.setArrivalAirport(arr);
            vo.setFlightDate(first.getFlightDate());
            vo.setTotalAmount(group.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));

            vo.setTickets(group.stream().map(t -> {
                TicketInOrderVO item = new TicketInOrderVO();
                item.setTicketId(t.getTicketId());
                item.setPassengerName("乘客"); // 暂时拿不到乘客名
                item.setCabinClass(t.getCabinClass().name());
                item.setFinalPrice(t.getPrice());
                item.setStatus(t.getStatus().name());
                return item;
            }).collect(Collectors.toList()));

            result.add(vo);
        }
        return result;
    }

    // --- 辅助方法 ---

    private TicketVO convertToTicketVO(Ticket ticket, FlightSearchVO flight, String passengerName) {
        TicketVO vo = new TicketVO();
        vo.setTicketId(ticket.getTicketId());
        vo.setFlightNumber(ticket.getFlightNumber());
        vo.setDepartureAirport(flight.getDepartureAirport());
        vo.setArrivalAirport(flight.getArrivalAirport());
        vo.setFlightDate(ticket.getFlightDate());
        vo.setPassengerName(passengerName);
        vo.setCabinClass(ticket.getCabinClass().name());
        vo.setFinalPrice(ticket.getPrice());
        vo.setStatus(ticket.getStatus().name());
        vo.setBookingTime(ticket.getBookingTime());
        return vo;
    }

    private BigDecimal applyMembershipDiscount(BigDecimal price, MembershipLevel level) {
        if (level == null) return price;
        BigDecimal discountRate = switch (level) {
            case 白金 -> new BigDecimal("0.85");
            case 金卡 -> new BigDecimal("0.90");
            case 银卡 -> new BigDecimal("0.95");
            default -> BigDecimal.ONE;
        };
        return price.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }
}