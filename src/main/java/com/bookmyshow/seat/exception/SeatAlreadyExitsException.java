package com.bookmyshow.seat.exception;

public class SeatAlreadyExitsException extends RuntimeException {
    public SeatAlreadyExitsException(String message) {
        super(message);
    }
}
