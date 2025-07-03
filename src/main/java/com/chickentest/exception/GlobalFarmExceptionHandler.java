package com.chickentest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalFarmExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalFarmExceptionHandler.class);

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<String> handleArticleNotFoundException(ArticleNotFoundException ex, WebRequest request) {
        logger.warn("{} - {}: {}", HttpStatus.NOT_FOUND.value(), request.getDescription(false), ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({InsufficientBalanceException.class, InsufficientStockException.class, MaxStockExceededException.class})
    public ResponseEntity<String> handleBadRequestBusinessExceptions(RuntimeException ex, WebRequest request) {
        logger.warn("{} - {}: {}", HttpStatus.BAD_REQUEST.value(), request.getDescription(false), ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class) // For validation errors like negative quantity/balance
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("{} - {}: {}", HttpStatus.BAD_REQUEST.value(), request.getDescription(false), ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FarmException.class) // General business logic errors from the farm services
    public ResponseEntity<String> handleFarmException(FarmException ex, WebRequest request) {
        // FarmException currently has @ResponseStatus(INTERNAL_SERVER_ERROR)
        // This handler can override that to BAD_REQUEST if most FarmExceptions are client errors.
        // If some FarmExceptions are truly server errors, they might need separate handling or specific status codes.
        logger.warn("{} - {}: {}", HttpStatus.BAD_REQUEST.value(), request.getDescription(false), ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Catch-all for any other unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("{} - {}: {}", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getDescription(false), ex.getMessage(), ex);
        return new ResponseEntity<>("An unexpected internal server error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
