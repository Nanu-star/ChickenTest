package com.chickentest.farm.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalFarmExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalFarmExceptionHandler.class);

  @ExceptionHandler(ArticleNotFoundException.class)
  public ResponseEntity<String> handleArticleNotFoundException(
      ArticleNotFoundException ex, WebRequest request) {
    logger.warn(
        "{} - {}: {}",
        HttpStatus.NOT_FOUND.value(),
        request.getDescription(false),
        ex.getMessage());
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({
    InsufficientBalanceException.class,
    InsufficientStockException.class,
    MaxStockExceededException.class
  })
  public ResponseEntity<String> handleBadRequestBusinessExceptions(
      RuntimeException ex, WebRequest request) {
    logger.warn(
        "{} - {}: {}",
        HttpStatus.BAD_REQUEST.value(),
        request.getDescription(false),
        ex.getMessage());
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(
      IllegalArgumentException.class) // For validation errors like negative quantity/balance
  public ResponseEntity<String> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    logger.warn(
        "{} - {}: {}",
        HttpStatus.BAD_REQUEST.value(),
        request.getDescription(false),
        ex.getMessage());
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(FarmException.class) // General business logic errors from the farm services
  public ResponseEntity<String> handleFarmException(FarmException ex, WebRequest request) {
    logger.warn(
        "{} - {}: {}",
        HttpStatus.BAD_REQUEST.value(),
        request.getDescription(false),
        ex.getMessage());
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  // @PreAuthorize denegado: 403 sin detalle de la política interna
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
    logger.warn(
        "{} - {}: {}",
        HttpStatus.FORBIDDEN.value(),
        request.getDescription(false),
        ex.getMessage());
    return new ResponseEntity<>("Access denied", HttpStatus.FORBIDDEN);
  }

  // Errores de Spring MVC (validación, JSON mal formado, método no permitido...): el cuerpo
  // se deja vacío, pero el motivo real queda en el log
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      @NonNull Exception ex,
      @Nullable Object body,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode statusCode,
      @NonNull WebRequest request) {
    logger.warn("{} - {}: {}", statusCode.value(), request.getDescription(false), ex.getMessage());
    return super.handleExceptionInternal(ex, body, headers, statusCode, request);
  }

  // Catch-all para cualquier otra excepción: mensaje genérico, traza completa solo en el log
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleGlobalException(Exception ex, WebRequest request) {
    logger.error(
        "{} - {}: {}",
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        request.getDescription(false),
        ex.getMessage(),
        ex);
    return new ResponseEntity<>(
        "An unexpected internal server error occurred. Please try again later.",
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
