package com.nimeshkmr.matching_engine.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nimeshkmr.core.DuplicateOrderIDException;
import com.nimeshkmr.core.OrderNotFoundException;
import com.nimeshkmr.core.UnauthorizedOrderCancellationException;
import com.nimeshkmr.matching_engine.web.dto.ErrorResponse;


@RestControllerAdvice
public class GlobalExceptionHandler {
    
    
    @ExceptionHandler(DuplicateOrderIDException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateOrderID(DuplicateOrderIDException exception) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "DUPLICATE_ORDER_ID",
            exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "ORDER_NOT_FOUND",
            exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedOrderCancellationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedCancellation(
            UnauthorizedOrderCancellationException exception) {

        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "UNAUTHORIZED_ORDER_CANCELLATION",
            exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
