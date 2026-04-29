package com.deskflow.ticketservice.exception;

import com.deskflow.ticketservice.dto.response.ErrorResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private ResponseEntity<ErrorResponse> mapToErrorResponse(
      HttpStatus httpStatus, String message, Map<String, String> errors) {
    return new ResponseEntity<>(
        new ErrorResponse(httpStatus.value(), httpStatus.getReasonPhrase(), message, errors),
        httpStatus);
  }

  // Generic Handler
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    return mapToErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
  }

  // 400 Bad Request
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    return mapToErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
    return mapToErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
  }

  // 403 Forbidden
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex) {
    return mapToErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null);
  }

  // 404 Not Found
  @ExceptionHandler(TicketNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTicketNotFoundException(TicketNotFoundException ex) {
    return mapToErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
  }

  // 409 Conflict
  @ExceptionHandler(InvalidStatusTransitionException.class)
  public ResponseEntity<ErrorResponse> handleInvalidStatusTransitionException(
      InvalidStatusTransitionException ex) {
    return mapToErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
  }
}
