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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired; // 必须引入
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
@Slf4j
// @RequiredArgsConstructor // 必须注释掉
public class TicketServiceImpl implements TicketService {

    // 全部加上 @Autowired 并去掉 final
    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PricingStrategy pricingStrategy;

    @Override
    @Transactional
    public List<TicketVO> createTickets(BookingRequest request, Integer bookerId) {
        log.info("正在为预订人ID {} 创建航班 {} 的机票", bookerId, request.getFlightNumber());

        Flight flight = flightRepository.findByIdWithLock(request.getFlightNumber())
                .orElseThrow(() -> new BusinessException("航班不存在: " + request.getFlightNumber()));

        Customer booker = customerRepository.findById(bookerId)
                .orElseThrow(() -> new BusinessException("预订人信息不存在: " + bookerId));

        Optional<Customer> passenger = customerRepository.findById(Integer.parseInt(request.getPassengerIds().get(0)));
        if (passenger.isEmpty()) {
            throw new BusinessException("部分或全部乘机人信息不存在");
        }

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
        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = new Ticket();
        BigDecimal basePrice = flight.getBasePrice();
        if (basePrice == null) {
            throw new BusinessException("航班 " + flight.getFlightNumber() + " 价格信息缺失，无法预订。");
        }

        BigDecimal finalPrice = applyMembershipDiscount(basePrice, booker.getMembershipLevel());

        ticket.setFlight(flight);
        ticket.setPassenger(passenger.get());
        ticket.setFlightDate(request.getFlightDate());
        ticket.setCabinClass(request.getCabinClass());
        ticket.setPrice(finalPrice);
        ticket.setStatus(TicketStatus.已支付);
        ticket.setBookingTime(now);
        ticket.setPaymentTime(now);

        createdTickets.add(ticket);

        List<Ticket> savedTickets = ticketRepository.saveAll(createdTickets);
        log.info("成功创建并支付了 {} 张机票。", savedTickets.size());

        return savedTickets.stream().map(this::convertToTicketVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void refundTicket(Long ticketId, String username) {
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
        ticket.setPaymentTime(null);

        ticketRepository.save(ticket);
    }

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