package com.bookmyshow.seat.repository;

import com.bookmyshow.seat.entity.SeatStatus;
import com.bookmyshow.seat.entity.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowSeat s WHERE s.showId = :showId AND s.seatId IN :seatIds")
    List<ShowSeat> findByShowIdAndSeatIdInForUpdate(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds);

    List<ShowSeat> findByStatusAndLockExpiryTimeBefore(SeatStatus status, LocalDateTime time);
}
