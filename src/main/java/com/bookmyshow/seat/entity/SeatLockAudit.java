package com.bookmyshow.seat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "seat_lock_audit",
        indexes = {
                @Index(name = "idx_audit_show_id", columnList = "show_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLockAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long showId;

    private Long seatId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private SeatStatus previousStatus;

    @Enumerated(EnumType.STRING)
    private SeatStatus newStatus;

    private LocalDateTime actionTime;
}
