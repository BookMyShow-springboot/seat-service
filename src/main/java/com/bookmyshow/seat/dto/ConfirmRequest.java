package com.bookmyshow.seat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotEmpty(message = "seatIds are required")
    private List<Long> seatIds;

    @NotNull(message = "bookingId is required")
    private Long bookingId;
}
