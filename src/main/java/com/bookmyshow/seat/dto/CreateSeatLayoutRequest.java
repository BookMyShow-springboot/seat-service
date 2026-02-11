package com.bookmyshow.seat.dto;

import com.bookmyshow.seat.entity.SeatType;
import lombok.Data;

import java.util.Map;

@Data
public class CreateSeatLayoutRequest {

    private Long screenId;
    private Integer rows;
    private Integer seatsPerRow;

    /**
     * Row letter (e.g., "A") -> seat type (e.g., PLATINUM).
     */
    private Map<String, SeatType> seatTypeMapping;
}
