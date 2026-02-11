package com.bookmyshow.seat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_screen_row_seat",
                        columnNames = {"screen_id", "row_label", "seat_number"}
                )
        },
        indexes = {
                @Index(name = "idx_seat_screen_id", columnList = "screen_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference from Show Service
    @Column(name = "screen_id", nullable = false)
    private Long screenId;

    @Column(name = "row_label", nullable = false, length = 5)
    private String rowLabel; // A, B, C

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatType seatType;
}
