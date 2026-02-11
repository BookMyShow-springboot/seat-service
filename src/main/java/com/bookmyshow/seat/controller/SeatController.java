package com.bookmyshow.seat.controller;

import com.bookmyshow.seat.dto.CreateSeatLayoutRequest;
import com.bookmyshow.seat.service.SeatLayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatLayoutService seatLayoutService;

    @PostMapping("/layout")
    public ResponseEntity<Void> createSeatLayout(@Valid @RequestBody CreateSeatLayoutRequest request) {
        seatLayoutService.createSeatLayout(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
