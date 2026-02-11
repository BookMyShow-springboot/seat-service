package com.bookmyshow.seat.scheduler;

import com.bookmyshow.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LockExpiryScheduler {

    private final SeatService seatService;

    @Scheduled(fixedRate = 60000)
    public void releaseExpiredLocks() {
        seatService.releaseExpiredLocks();
    }
}
