package com.bookmyshow.seat.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        log.warn("Payload validation failed path={}, errorCount={}",
                request.getRequestURI(), ex.getBindingResult().getErrorCount());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        fieldErrors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid request payload")
                .path(request.getRequestURI())
                .validationErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(SeatAlreadyExitsException.class)
    public ResponseEntity<ErrorResponse> handleSeatAlreadyExits(SeatAlreadyExitsException ex,
                                                                HttpServletRequest request){
        log.warn("Seat layout already exists path={}", request.getRequestURI());

        ErrorResponse errorResponse= ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Seat Layout Already Exists")
                .message(ex.getMessage())
                .path(request.getRequestURI())

                .build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(InvalidSeatLayoutException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSeatLayout(InvalidSeatLayoutException ex,
                                                                 HttpServletRequest request) {
        log.warn("Invalid seat layout path={}, message={}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Seat Layout")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

}
