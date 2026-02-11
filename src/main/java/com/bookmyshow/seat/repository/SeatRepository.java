package com.bookmyshow.seat.repository;

import com.bookmyshow.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByScreenId(Long screenId);

    boolean existsByScreenId(Long screenId);
}
