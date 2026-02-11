package com.bookmyshow.seat.service.impl;


import com.bookmyshow.seat.dto.CreateSeatLayoutRequest;
import com.bookmyshow.seat.entity.Seat;
import com.bookmyshow.seat.entity.SeatType;
import com.bookmyshow.seat.exception.InvalidSeatLayoutException;
import com.bookmyshow.seat.exception.SeatAlreadyExitsException;
import com.bookmyshow.seat.repository.SeatRepository;
import com.bookmyshow.seat.service.SeatLayoutService;
import com.bookmyshow.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService, SeatLayoutService {

    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public void createSeatLayout(CreateSeatLayoutRequest request) {

        if (request == null) {
            throw new InvalidSeatLayoutException("Request body is required");
        }
        if (request.getScreenId() == null) {
            throw new InvalidSeatLayoutException("screenId is required");
        }
        if (request.getRows() == null || request.getRows() < 1) {
            throw new InvalidSeatLayoutException("rows must be >= 1");
        }
        if (request.getSeatsPerRow() == null || request.getSeatsPerRow() < 1) {
            throw new InvalidSeatLayoutException("seatsPerRow must be >= 1");
        }
        if (request.getSeatTypeMapping() == null || request.getSeatTypeMapping().isEmpty()) {
            throw new InvalidSeatLayoutException("seatTypeMapping is required");
        }

        Long screenId = request.getScreenId();

        if (seatRepository.existsByScreenId(screenId)) {
            throw new SeatAlreadyExitsException("Seat layout already exists for screenId=" + screenId);
        }

        List<String> expectedRows = generateRowLabels(request.getRows());
        Map<String, SeatType> normalizedMapping = normalizeAndValidateMapping(expectedRows, request.getSeatTypeMapping());

        int totalSeats = Math.multiplyExact(request.getRows(), request.getSeatsPerRow());
        List<Seat> seats = new ArrayList<>(totalSeats);

        for (String rowLabel : expectedRows) {
            SeatType seatType = normalizedMapping.get(rowLabel);
            for (int seatNumber = 1; seatNumber <= request.getSeatsPerRow(); seatNumber++) {
                seats.add(Seat.builder()
                        .screenId(screenId)
                        .rowLabel(rowLabel)
                        .seatNumber(seatNumber)
                        .seatType(seatType)
                        .build());
            }
        }

        seatRepository.saveAll(seats);
    }

    private static List<String> generateRowLabels(int rows) {
        // A, B, C...; supports AA, AB... if rows > 26
        List<String> labels = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            labels.add(toExcelStyleLabel(i));
        }
        return labels;
    }

    private static String toExcelStyleLabel(int indexZeroBased) {
        int n = indexZeroBased + 1;
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            int rem = (n - 1) % 26;
            sb.append((char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return sb.reverse().toString();
    }

    private static Map<String, SeatType> normalizeAndValidateMapping(
            List<String> expectedRows,
            Map<String, SeatType> seatTypeMapping
    ) {
        Set<String> expected = new LinkedHashSet<>(expectedRows);
        Map<String, SeatType> normalized = new HashMap<>();

        for (Map.Entry<String, SeatType> entry : seatTypeMapping.entrySet()) {
            String rawKey = entry.getKey();
            if (rawKey == null) {
                throw new InvalidSeatLayoutException("seatTypeMapping contains a null row key");
            }
            String key = rawKey.trim().toUpperCase(Locale.ROOT);
            if (key.isEmpty()) {
                throw new InvalidSeatLayoutException("seatTypeMapping contains an empty row key");
            }
            SeatType type = entry.getValue();
            if (type == null) {
                throw new InvalidSeatLayoutException("seatTypeMapping contains a null SeatType for row " + key);
            }
            normalized.put(key, type);
        }

        // Must match expected rows exactly (no missing, no extra)
        List<String> missing = expected.stream().filter(r -> !normalized.containsKey(r)).toList();
        if (!missing.isEmpty()) {
            throw new InvalidSeatLayoutException("seatTypeMapping missing rows: " + missing);
        }

        List<String> extras = normalized.keySet().stream().filter(k -> !expected.contains(k)).sorted().toList();
        if (!extras.isEmpty()) {
            throw new InvalidSeatLayoutException("seatTypeMapping has invalid rows: " + extras);
        }

        // Return mapping in expected row order (helps readability/debugging)
        Map<String, SeatType> ordered = new LinkedHashMap<>();
        for (String row : expectedRows) {
            ordered.put(row, normalized.get(row));
        }
        return ordered;
    }
}
