package com.bookmyshow.seat.service;

import com.bookmyshow.seat.dto.*;
import com.bookmyshow.seat.entity.Seat;
import com.bookmyshow.seat.entity.SeatStatus;
import com.bookmyshow.seat.entity.Show;
import com.bookmyshow.seat.entity.ShowSeat;
import com.bookmyshow.seat.exception.SeatLockConflictException;
import com.bookmyshow.seat.exception.SeatNotAvailableException;
import com.bookmyshow.seat.exception.ShowNotFoundException;
import com.bookmyshow.seat.repository.SeatRepository;
import com.bookmyshow.seat.repository.ShowRepository;
import com.bookmyshow.seat.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatRepository seatRepository;

    @Value("${seat-service.lock-duration-minutes:10}")
    private int lockDurationMinutes;

    public SeatLayoutResponse getSeatLayout(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));

        List<ShowSeat> showSeats = showSeatRepository.findByShowId(showId);
        if (showSeats.isEmpty()) {
            return SeatLayoutResponse.builder()
                    .showId(showId)
                    .seats(List.of())
                    .build();
        }

        List<Long> seatIds = showSeats.stream().map(ShowSeat::getSeatId).distinct().toList();
        Map<Long, Seat> seatMap = seatRepository.findAllById(seatIds).stream()
                .collect(Collectors.toMap(Seat::getId, s -> s));

        List<SeatLayoutResponse.SeatItem> items = showSeats.stream()
                .map(ss -> {
                    Seat seat = seatMap.get(ss.getSeatId());
                    return SeatLayoutResponse.SeatItem.builder()
                            .seatId(ss.getSeatId())
                            .row(seat != null ? seat.getRowLabel() : null)
                            .number(seat != null ? seat.getSeatNumber() : null)
                            .type(seat != null && seat.getSeatType() != null ? seat.getSeatType().name() : null)
                            .status(ss.getStatus().name())
                            .build();
                })
                .toList();

        return SeatLayoutResponse.builder()
                .showId(showId)
                .seats(items)
                .build();
    }

    @Transactional
    public LockResponse lockSeats(Long showId, LockRequest request) {
        validateShowExists(showId);

        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdInForUpdate(showId, request.getSeatIds());

        if (showSeats.size() != request.getSeatIds().size()) {
            throw new SeatLockConflictException("One or more seats not found for this show");
        }

        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatNotAvailableException("Some seats already booked or locked");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusMinutes(lockDurationMinutes);

        for (ShowSeat ss : showSeats) {
            ss.setStatus(SeatStatus.LOCKED);
            ss.setLockedByUserId(request.getUserId());
            ss.setLockedAt(now);
            ss.setLockExpiryTime(expiry);
        }
        showSeatRepository.saveAll(showSeats);

        return LockResponse.builder()
                .message("Seats locked successfully")
                .lockExpiryTime(expiry.format(ISO_FORMAT))
                .build();
    }

    @Transactional
    public ConfirmResponse confirmSeats(Long showId, ConfirmRequest request) {
        validateShowExists(showId);

        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdInForUpdate(showId, request.getSeatIds());

        if (showSeats.size() != request.getSeatIds().size()) {
            throw new SeatLockConflictException("One or more seats not found for this show");
        }

        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() != SeatStatus.LOCKED || !request.getUserId().equals(ss.getLockedByUserId())) {
                throw new SeatLockConflictException("Seats must be locked by this user to confirm");
            }
        }

        for (ShowSeat ss : showSeats) {
            ss.setStatus(SeatStatus.BOOKED);
            ss.setLockedByUserId(null);
            ss.setLockedAt(null);
            ss.setLockExpiryTime(null);
        }
        showSeatRepository.saveAll(showSeats);

        return ConfirmResponse.builder()
                .message("Seats booked successfully")
                .build();
    }

    @Transactional
    public ReleaseResponse releaseSeats(Long showId, ReleaseRequest request) {
        validateShowExists(showId);

        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdInForUpdate(showId, request.getSeatIds());

        if (showSeats.size() != request.getSeatIds().size()) {
            throw new SeatLockConflictException("One or more seats not found for this show");
        }

        for (ShowSeat ss : showSeats) {
            if (ss.getStatus() != SeatStatus.LOCKED || !request.getUserId().equals(ss.getLockedByUserId())) {
                throw new SeatLockConflictException("Seats must be locked by this user to release");
            }
        }

        for (ShowSeat ss : showSeats) {
            ss.setStatus(SeatStatus.AVAILABLE);
            ss.setLockedByUserId(null);
            ss.setLockedAt(null);
            ss.setLockExpiryTime(null);
        }
        showSeatRepository.saveAll(showSeats);

        return ReleaseResponse.builder()
                .message("Seats released successfully")
                .build();
    }

    @Transactional
    public void releaseExpiredLocks() {
        List<ShowSeat> expired = showSeatRepository.findByStatusAndLockExpiryTimeBefore(SeatStatus.LOCKED, LocalDateTime.now());
        for (ShowSeat ss : expired) {
            ss.setStatus(SeatStatus.AVAILABLE);
            ss.setLockedByUserId(null);
            ss.setLockedAt(null);
            ss.setLockExpiryTime(null);
        }
        if (!expired.isEmpty()) {
            showSeatRepository.saveAll(expired);
        }
    }

    private void validateShowExists(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new ShowNotFoundException(showId);
        }
    }
}
