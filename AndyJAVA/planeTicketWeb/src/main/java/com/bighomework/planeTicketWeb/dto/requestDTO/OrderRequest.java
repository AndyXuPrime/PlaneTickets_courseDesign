package com.bighomework.planeTicketWeb.dto.requestDTO;

import com.bighomework.planeTicketWeb.enums.CabinClass;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "航班号不能为空")
    private String flightNumber;

    @NotNull(message = "航班日期不能为空")
    @Future(message = "航班日期必须是未来日期")
    private LocalDate flightDate;

    @NotEmpty(message = "必须至少有一位乘机人")
    private List<Integer> passengerIds;

    @NotNull(message = "必须选择舱位")
    private CabinClass cabinClass;
}