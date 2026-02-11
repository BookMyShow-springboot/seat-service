package com.bookmyshow.seat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "show_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_show_seat",
                        columnNames = {"show_id", "seat_id"}
                )
        },
        indexes = {
                @Index(name = "idx_show_seat_show_id", columnList = "show_id"),
                @Index(name = "idx_show_seat_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference from Show Service
    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status;

    private Long lockedByUserId;

    private LocalDateTime lockedAt;

    private LocalDateTime lockExpiryTime;

    private Long bookingId;

    // For optimistic locking
    @Version
    private Integer version;
}
