package com.bighomework.order.service.impl;

import com.bighomework.common.dto.requestDTO.BookingRequest;
import com.bighomework.common.dto.responseDTO.*;
import com.bighomework.common.enums.*;
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
import java.util.*;
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
        // 打印前端传来的名字，确保接收到了
        log.info(">>> [Booking Start] User: {}, Names: {}", username, request.getPassengerNames());

        // 1. 获取订票人信息 (为了拿 UserID 绑定订单)
        LoginResponse booker = fetchBookerInfo(username);

        // 2. 获取航班信息 (为了拿价格)
        FlightSearchVO flight = fetchFlightInfo(request.getFlightNumber());

        // 3. 构建机票 (直接使用前端传来的名字)
        List<Ticket> ticketsToSave = buildTickets(request, booker, flight);

        // 4. 保存入库
        try {
            List<Ticket> saved = ticketRepository.saveAll(ticketsToSave);
            log.info(">>> [Booking Success] Saved {} tickets.", saved.size());

            final FlightSearchVO finalFlight = flight;
            return saved.stream()
                    .map(t -> convertToTicketVO(t, finalFlight, t.getPassengerName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("!!! [DB Save Failed] !!!", e);
            throw new BusinessException("Failed to save order: " + e.getMessage());
        }
    }

    // 逻辑简化：直接遍历名字列表，不再进行 ID 匹配
    private List<Ticket> buildTickets(BookingRequest req, LoginResponse booker, FlightSearchVO flight) {
        BigDecimal finalPrice = applyMembershipDiscount(flight.getPrice(), booker.getMembershipLevel());
        List<Ticket> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (String name : req.getPassengerNames()) {
            Ticket ticket = new Ticket();
            ticket.setFlightNumber(req.getFlightNumber());
            ticket.setFlightDate(req.getFlightDate());
            ticket.setUserId(booker.getId()); // 订单归属于订票人
            ticket.setCabinClass(req.getCabinClass());
            ticket.setPrice(finalPrice);
            ticket.setStatus(TicketStatus.已支付);
            ticket.setBookingTime(now);
            ticket.setPaymentTime(now);

            // 直接设置名字
            ticket.setPassengerName(name);

            list.add(ticket);
        }
        return list;
    }

    @Override
    @Transactional
    public void refundTicket(Long ticketId, String username) {
        // ... (保持之前的修复版本不变)
        LoginResponse user = fetchBookerInfo(username);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Ticket not found"));

        if (!ticket.getUserId().equals(user.getId())) {
            throw new BusinessException("You can only refund your own tickets.");
        }
        if (ticket.getStatus() == TicketStatus.已取消) {
            throw new BusinessException("Ticket is already cancelled.");
        }

        ticket.setStatus(TicketStatus.已取消);
        ticket.setPaymentTime(null);
        ticketRepository.save(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogicalOrderVO> getMyOrders(String username) {
        // ... (保持不变)
        try {
            LoginResponse userResp = fetchBookerInfo(username);
            List<Ticket> tickets = ticketRepository.findByUserId(userResp.getId());

            Map<String, List<Ticket>> grouped = tickets.stream().collect(Collectors.groupingBy(t ->
                    (t.getBookingTime() != null ? t.getBookingTime().toString() : "0") + "@" + t.getFlightNumber()));

            return grouped.values().stream().map(group -> {
                        Ticket first = group.get(0);
                        LogicalOrderVO vo = new LogicalOrderVO();
                        vo.setLogicalOrderId(first.getTicketId().toString());
                        vo.setFlightNumber(first.getFlightNumber());
                        vo.setFlightDate(first.getFlightDate());
                        vo.setTotalAmount(group.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
                        vo.setTickets(group.stream().map(this::convertToTicketInOrderVO).collect(Collectors.toList()));
                        return vo;
                    }).sorted((a, b) -> b.getFlightDate().compareTo(a.getFlightDate()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // --- Helper Methods ---

    private LoginResponse fetchBookerInfo(String username) {
        ApiResponse<LoginResponse> resp = authFeignClient.getUserByPhone(username);
        if (resp == null || resp.getData() == null) throw new BusinessException("User not found via Auth Service");
        return resp.getData();
    }

    // 注意：fetchFamilyList 方法已经不需要了，可以删掉

    private FlightSearchVO fetchFlightInfo(String flightNumber) {
        ApiResponse<FlightSearchVO> resp = flightFeignClient.getFlightByNumber(flightNumber);
        if (resp == null || resp.getData() == null) throw new BusinessException("Flight not found via Flight Service");
        return resp.getData();
    }

    private BigDecimal applyMembershipDiscount(BigDecimal price, MembershipLevel level) {
        if (price == null) return BigDecimal.ZERO;
        double rate = switch (level != null ? level : MembershipLevel.普通) {
            case 白金 -> 0.85; case 金卡 -> 0.90; case 银卡 -> 0.95; default -> 1.0;
        };
        return price.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP);
    }

    private TicketVO convertToTicketVO(Ticket t, FlightSearchVO f, String pName) {
        TicketVO vo = new TicketVO();
        vo.setTicketId(t.getTicketId());
        vo.setFlightNumber(t.getFlightNumber());
        vo.setDepartureAirport(f != null ? f.getDepartureAirport() : "Unknown");
        vo.setArrivalAirport(f != null ? f.getArrivalAirport() : "Unknown");
        vo.setFlightDate(t.getFlightDate());
        vo.setPassengerName(pName);
        vo.setCabinClass(t.getCabinClass().name());
        vo.setFinalPrice(t.getPrice());
        vo.setStatus(t.getStatus().name());
        vo.setBookingTime(t.getBookingTime());
        return vo;
    }

    private TicketInOrderVO convertToTicketInOrderVO(Ticket t) {
        TicketInOrderVO item = new TicketInOrderVO();
        item.setTicketId(t.getTicketId());
        item.setPassengerName(t.getPassengerName());
        item.setCabinClass(t.getCabinClass().name());
        item.setFinalPrice(t.getPrice());
        item.setStatus(t.getStatus().name());
        return item;
    }
}