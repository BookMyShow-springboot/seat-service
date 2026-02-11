package com.bookmyshow.seat.exception;

public class SeatLockConflictException extends RuntimeException {

    public SeatLockConflictException(String message) {
        super(message);
    }

    public SeatLockConflictException() {
        super("Some seats already booked or locked");
    }
}
