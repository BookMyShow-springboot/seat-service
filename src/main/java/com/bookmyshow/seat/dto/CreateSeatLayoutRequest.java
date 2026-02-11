package com.bookmyshow.seat.dto;

import com.bookmyshow.seat.entity.SeatType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CreateSeatLayoutRequest {

    @NotNull(message = "screenId is required")
    private Long screenId;

    @NotNull(message = "rows is required")
    @Min(value = 1, message = "rows must be >= 1")
    private Integer rows;

    @NotNull(message = "seatsPerRow is required")
    @Min(value = 1, message = "seatsPerRow must be >= 1")
    private Integer seatsPerRow;

    /**
     * Row letter (e.g., "A") -> seat type (e.g., PLATINUM).
     */
    @NotEmpty(message = "seatTypeMapping is required")
    private Map<String, SeatType> seatTypeMapping;
}
