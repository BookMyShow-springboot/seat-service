package com.bookmyshow.seat.integration;

import com.bookmyshow.seat.dto.*;
import com.bookmyshow.seat.entity.*;
import com.bookmyshow.seat.exception.SeatLockConflictException;
import com.bookmyshow.seat.exception.SeatNotAvailableException;
import com.bookmyshow.seat.exception.ShowNotFoundException;
import com.bookmyshow.seat.repository.*;
import com.bookmyshow.seat.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class SeatServiceIntegrationTest {

    @Autowired
    private SeatService seatService;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ScreenRepository screenRepository;

    private Long showId;
    private Long screenId;
    private Long seat1Id;
    private Long seat2Id;

    @BeforeEach
    void setUp() {
        showSeatRepository.deleteAll();
        seatRepository.deleteAll();
        showRepository.deleteAll();
        screenRepository.deleteAll();

        Screen screen = new Screen();
        screen.setName("Screen 1");
        screen.setTheatreId(100L);
        screen.setTotalRows(5);
        screen.setTotalColumns(10);
        screen = screenRepository.save(screen);
        screenId = screen.getId();

        Show show = new Show();
        show.setScreenId(screenId);
        show.setMovieId(200L);
        show.setStartTime(LocalDateTime.now().plusHours(1));
        show.setEndTime(LocalDateTime.now().plusHours(3));
        show = showRepository.save(show);
        showId = show.getId();

        Seat seat1 = new Seat();
        seat1.setScreenId(screenId);
        seat1.setRowLabel("A");
        seat1.setSeatNumber(1);
        seat1.setSeatType(SeatType.GOLD);
        seat1 = seatRepository.save(seat1);
        seat1Id = seat1.getId();

        Seat seat2 = new Seat();
        seat2.setScreenId(screenId);
        seat2.setRowLabel("A");
        seat2.setSeatNumber(2);
        seat2.setSeatType(SeatType.GOLD);
        seat2 = seatRepository.save(seat2);
        seat2Id = seat2.getId();

        ShowSeat showSeat1 = new ShowSeat();
        showSeat1.setShowId(showId);
        showSeat1.setSeatId(seat1Id);
        showSeat1.setStatus(SeatStatus.AVAILABLE);
        showSeatRepository.save(showSeat1);

        ShowSeat showSeat2 = new ShowSeat();
        showSeat2.setShowId(showId);
        showSeat2.setSeatId(seat2Id);
        showSeat2.setStatus(SeatStatus.AVAILABLE);
        showSeatRepository.save(showSeat2);
    }

    @Test
    void getSeatLayout_returnsSeatsForShow() {
        SeatLayoutResponse response = seatService.getSeatLayout(showId);

        assertThat(response.getShowId()).isEqualTo(showId);
        assertThat(response.getSeats()).hasSize(2);
        assertThat(response.getSeats())
                .extracting(SeatLayoutResponse.SeatItem::getStatus)
                .containsExactlyInAnyOrder("AVAILABLE", "AVAILABLE");
    }

    @Test
    void getSeatLayout_throwsWhenShowNotFound() {
        assertThatThrownBy(() -> seatService.getSeatLayout(999L))
                .isInstanceOf(ShowNotFoundException.class);
    }

    @Test
    void lockSeats_locksSeatsAndReturnsExpiry() {
        LockRequest request = new LockRequest(12L, List.of(seat1Id, seat2Id));

        LockResponse response = seatService.lockSeats(showId, request);

        assertThat(response.getMessage()).isEqualTo("Seats locked successfully");
        assertThat(response.getLockExpiryTime()).isNotBlank();

        SeatLayoutResponse layout = seatService.getSeatLayout(showId);
        assertThat(layout.getSeats()).extracting(SeatLayoutResponse.SeatItem::getStatus)
                .containsExactlyInAnyOrder("LOCKED", "LOCKED");
    }

    @Test
    void lockSeats_secondUserGetsConflict() {
        LockRequest user1 = new LockRequest(10L, List.of(seat1Id, seat2Id));
        seatService.lockSeats(showId, user1);

        LockRequest user2 = new LockRequest(20L, List.of(seat1Id, seat2Id));

        assertThatThrownBy(() -> seatService.lockSeats(showId, user2))
                .isInstanceOf(SeatNotAvailableException.class)
                .hasMessageContaining("already booked or locked");
    }

    @Test
    void confirmSeats_afterLock_booksSeats() {
        LockRequest lockRequest = new LockRequest(12L, List.of(seat1Id, seat2Id));
        seatService.lockSeats(showId, lockRequest);

        ConfirmRequest confirmRequest = new ConfirmRequest(12L, List.of(seat1Id, seat2Id), 5001L);
        ConfirmResponse response = seatService.confirmSeats(showId, confirmRequest);

        assertThat(response.getMessage()).isEqualTo("Seats booked successfully");

        SeatLayoutResponse layout = seatService.getSeatLayout(showId);
        assertThat(layout.getSeats()).extracting(SeatLayoutResponse.SeatItem::getStatus)
                .containsExactlyInAnyOrder("BOOKED", "BOOKED");
    }

    @Test
    void confirmSeats_differentUserGetsConflict() {
        LockRequest lockRequest = new LockRequest(12L, List.of(seat1Id));
        seatService.lockSeats(showId, lockRequest);

        ConfirmRequest wrongUser = new ConfirmRequest(99L, List.of(seat1Id), 5001L);

        assertThatThrownBy(() -> seatService.confirmSeats(showId, wrongUser))
                .isInstanceOf(SeatLockConflictException.class);
    }

    @Test
    void releaseSeats_afterLock_makesSeatsAvailable() {
        LockRequest lockRequest = new LockRequest(12L, List.of(seat1Id, seat2Id));
        seatService.lockSeats(showId, lockRequest);

        ReleaseRequest releaseRequest = new ReleaseRequest(12L, List.of(seat1Id, seat2Id));
        ReleaseResponse response = seatService.releaseSeats(showId, releaseRequest);

        assertThat(response.getMessage()).isEqualTo("Seats released successfully");

        SeatLayoutResponse layout = seatService.getSeatLayout(showId);
        assertThat(layout.getSeats()).extracting(SeatLayoutResponse.SeatItem::getStatus)
                .containsExactlyInAnyOrder("AVAILABLE", "AVAILABLE");
    }

    @Test
    void releaseSeats_differentUserGetsConflict() {
        LockRequest lockRequest = new LockRequest(12L, List.of(seat1Id));
        seatService.lockSeats(showId, lockRequest);

        ReleaseRequest wrongUser = new ReleaseRequest(99L, List.of(seat1Id));

        assertThatThrownBy(() -> seatService.releaseSeats(showId, wrongUser))
                .isInstanceOf(SeatLockConflictException.class);
    }
}
