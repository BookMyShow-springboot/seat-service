package com.bookmyshow.seat.controller;

import com.bookmyshow.seat.dto.*;
import com.bookmyshow.seat.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/{showId}/seats")
    public ResponseEntity<SeatLayoutResponse> getSeats(@PathVariable Long showId) {
        SeatLayoutResponse response = seatService.getSeatLayout(showId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{showId}/lock")
    public ResponseEntity<LockResponse> lockSeats(
            @PathVariable Long showId,
            @Valid @RequestBody LockRequest request) {
        LockResponse response = seatService.lockSeats(showId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{showId}/confirm")
    public ResponseEntity<ConfirmResponse> confirmSeats(
            @PathVariable Long showId,
            @Valid @RequestBody ConfirmRequest request) {
        ConfirmResponse response = seatService.confirmSeats(showId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{showId}/release")
    public ResponseEntity<ReleaseResponse> releaseSeats(
            @PathVariable Long showId,
            @Valid @RequestBody ReleaseRequest request) {
        ReleaseResponse response = seatService.releaseSeats(showId, request);
        return ResponseEntity.ok(response);
    }
}
