package com.bookmyshow.seat.controller;

import com.bookmyshow.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;


}
