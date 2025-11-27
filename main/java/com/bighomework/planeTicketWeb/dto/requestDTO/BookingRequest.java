package com.bighomework.planeTicketWeb.dto.requestDTO;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.bighomework.planeTicketWeb.enums.CabinClass;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest implements Serializable {
    @NotNull
    private String flightNumber;
    @NotNull @Future
    private LocalDate flightDate;
    @NotEmpty
    private List<String> passengerIds;
    @NotNull
    private CabinClass cabinClass;
}