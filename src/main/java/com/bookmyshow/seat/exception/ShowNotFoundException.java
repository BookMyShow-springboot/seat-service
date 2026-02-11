package com.bookmyshow.seat.exception;

public class ShowNotFoundException extends RuntimeException {

    public ShowNotFoundException(String message) {
        super(message);
    }

    public ShowNotFoundException(Long showId) {
        super("Show not found: " + showId);
    }
}
