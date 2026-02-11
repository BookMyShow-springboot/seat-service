package com.bookmyshow.seat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long showId;

    private Long seatId;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private Long lockedByUserId;

    private LocalDateTime lockedAt;

    private LocalDateTime lockExpiryTime;

    @Version
    private Integer version;
}
