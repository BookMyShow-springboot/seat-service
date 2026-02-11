package com.bookmyshow.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLayoutResponse {

    private Long showId;
    private List<SeatItem> seats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatItem {
        private Long seatId;
        private String row;
        private Integer number;
        private String type;
        private String status;
    }
}
