package com.bighomework.common.dto.requestDTO;

import com.bighomework.common.enums.CabinClass;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest implements Serializable {
    @NotNull
    private String flightNumber;

    @NotNull @Future
    private LocalDate flightDate;

    // ✅ 关键修改：字段名改为 passengerNames，与前端一致
    @NotEmpty
    private List<String> passengerNames;

    @NotNull
    private CabinClass cabinClass;
}