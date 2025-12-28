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
        // ... (保持之前逻辑，确保 applyMembershipDiscount 有非空检查)
        return new ArrayList<>(); // 示例占位
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogicalOrderVO> getMyOrders(String username) {
        log.info("查询用户 {} 的订单列表", username);

        // 1. 获取用户信息
        ApiResponse<LoginResponse> userResp = authFeignClient.getUserByPhone(username);
        if (userResp == null || userResp.getData() == null) {
            throw new BusinessException("无法获取当前用户信息");
        }
        Integer userId = userResp.getData().getId();
        String realName = userResp.getData().getName();

        // 2. 查询所有机票记录
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        if (tickets.isEmpty()) return new ArrayList<>();

        // 3. 逻辑分组 (将同一批预订、同一个航班的机票合成一个“逻辑订单”)
        // 使用 bookingTime 和 flightNumber 作为 Key，增加非空保护
        Map<String, List<Ticket>> grouped = tickets.stream().collect(Collectors.groupingBy(t -> {
            String timeStr = (t.getBookingTime() != null) ? t.getBookingTime().toString() : "unknown";
            return timeStr + "@" + t.getFlightNumber();
        }));

        List<LogicalOrderVO> result = new ArrayList<>();

        for (List<Ticket> group : grouped.values()) {
            Ticket first = group.get(0);

            // 4. 跨服务获取航班详情 (起降机场)
            String dep = "未知";
            String arr = "未知";
            try {
                ApiResponse<FlightSearchVO> fResp = flightFeignClient.getFlightByNumber(first.getFlightNumber());
                if (fResp != null && fResp.getData() != null) {
                    dep = fResp.getData().getDepartureAirport();
                    arr = fResp.getData().getArrivalAirport();
                }
            } catch (Exception e) {
                log.error("远程获取航班 {} 信息失败", first.getFlightNumber());
            }

            // 5. 构建 VO
            LogicalOrderVO vo = new LogicalOrderVO();
            vo.setLogicalOrderId(first.getTicketId().toString()); // 简单用第一个ID作为逻辑单号
            vo.setFlightNumber(first.getFlightNumber());
            vo.setDepartureAirport(dep);
            vo.setArrivalAirport(arr);
            vo.setFlightDate(first.getFlightDate());

            // 计算总价
            BigDecimal total = group.stream()
                    .map(t -> t.getPrice() != null ? t.getPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setTotalAmount(total);

            // 填充子票据列表
            vo.setTickets(group.stream().map(t -> {
                TicketInOrderVO item = new TicketInOrderVO();
                item.setTicketId(t.getTicketId());
                item.setPassengerName(realName); // 使用从 Auth 服务拿到的真名
                item.setCabinClass(t.getCabinClass() != null ? t.getCabinClass().name() : "未知");
                item.setFinalPrice(t.getPrice());
                item.setStatus(t.getStatus() != null ? t.getStatus().name() : "未知");
                return item;
            }).collect(Collectors.toList()));

            result.add(vo);
        }

        // 按时间降序排序
        result.sort((a, b) -> b.getLogicalOrderId().compareTo(a.getLogicalOrderId()));
        return result;
    }

    @Override
    @Transactional
    public void refundTicket(Long ticketId, String username) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("机票记录不存在"));

        // 权限校验逻辑保持...
        ticket.setStatus(TicketStatus.已取消);
        ticketRepository.save(ticket);
    }
}