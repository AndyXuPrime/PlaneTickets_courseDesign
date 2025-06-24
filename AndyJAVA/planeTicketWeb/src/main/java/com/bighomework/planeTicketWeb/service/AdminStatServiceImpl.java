package com.bighomework.planeTicketWeb.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.bighomework.planeTicketWeb.dto.stats.CustomerAnalysisDTO;
import com.bighomework.planeTicketWeb.dto.stats.GenericStatDTO;
import com.bighomework.planeTicketWeb.dto.stats.RouteAnalysisDTO;
import com.bighomework.planeTicketWeb.dto.stats.RouteSalesDTO;
import com.bighomework.planeTicketWeb.exception.BusinessException;
import com.bighomework.planeTicketWeb.repository.CustomerRepository;
import com.bighomework.planeTicketWeb.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminStatServiceImpl implements AdminStatService {

    // 注入需要的 Repository
    private final CustomerRepository customerRepository;
    private final TicketRepository ticketRepository;

    @Override
    public Map<String, Object> analyzeCustomersByDimension(String dimension) {
        Map<String, Object> result = new HashMap<>();

        switch (dimension.toLowerCase()) {
            case "gender" -> {
                List<CustomerAnalysisDTO> genderAnalysis = customerRepository.analyzeByGender();
                result.put("title", "按性别分析");
                result.put("data", genderAnalysis);
            }

            case "age" -> {
                // 原生查询返回的是 List<Map<String, Object>>，需要手动转换
                List<Map<String, Object>> rawAgeAnalysis = customerRepository.analyzeByAgeGroupNative();
                List<CustomerAnalysisDTO> ageAnalysis = rawAgeAnalysis.stream()
                        .map(row -> new CustomerAnalysisDTO(
                                (String) row.get("dimension"),
                                ((Number) row.get("ticketCount")).longValue(),
                                ((Number) row.get("averageSpent")).doubleValue()
                        ))
                        .collect(Collectors.toList());
                result.put("title", "按年龄段分析");
                result.put("data", ageAnalysis);
            }

            default -> throw new BusinessException("不支持的分析维度: " + dimension + "。支持的维度: 'gender', 'age'");
        }
        // 可以扩展更多维度，如 'membership'
        // case "membership": ...
        return result;
    }

    @Override
    public Map<String, Object> analyzeRoutes() {
        // 获取最热门的10条航线
        PageRequest topTen = PageRequest.of(0, 10);
        List<RouteAnalysisDTO> topRoutes = ticketRepository.findTopHotRoutes(topTen);

        Map<String, Object> result = new HashMap<>();
        result.put("title", "Top 10 热门航线");
        result.put("data", topRoutes);

        // 可以在这里添加更多航线相关的分析，比如最受欢迎的航空公司等
        // List<Map<String, Object>> topAirlines = ...

        return result;
    }

     @Override
    public Map<String, Object> getRouteSalesAnalysis(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<RouteSalesDTO> salesData = ticketRepository.findRouteSalesBetweenDates(startDateTime, endDateTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("title", "航线销售额分析 (" + startDate + " to " + endDate + ")");
        result.put("data", salesData);
        
        // 简单的打折策略建议
        String suggestion = salesData.isEmpty() ? "无销售数据，无法生成策略。" : 
            "建议对销售额较低或票量较少的航线（如：" + salesData.get(salesData.size()-1).getDepartureAirport() + " -> " + salesData.get(salesData.size()-1).getArrivalAirport() + "）推出限时折扣活动。";
        result.put("suggestion", suggestion);
        
        return result;
    }

    @Override
    public List<GenericStatDTO> getStatsReport(String dimension) {
        return switch (dimension.toLowerCase()) {
            case "airline" -> ticketRepository.findStatsByAirline();
            case "aircraftmodel" -> ticketRepository.findStatsByAircraftModel();
            // "route" 维度可以使用已有的 findTopHotRoutes
            case "route" -> ticketRepository.findTopHotRoutes(PageRequest.of(0, 20)).stream()
                                .map(r -> new GenericStatDTO(r.getDepartureAirport() + "-" + r.getArrivalAirport(), r.getTicketCount()))
                                .collect(Collectors.toList());
            default -> throw new BusinessException("不支持的统计维度: " + dimension);
        };
    }
}