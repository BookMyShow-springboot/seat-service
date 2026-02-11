package com.bookmyshow.seat.repository;

import com.bookmyshow.seat.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Long> {

    boolean existsByIdAndScreenId(Long id, Long screenId);
}
