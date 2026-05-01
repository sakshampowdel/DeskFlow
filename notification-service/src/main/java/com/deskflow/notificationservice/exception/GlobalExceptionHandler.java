package com.deskflow.notificationservice.exception;

import com.deskflow.notificationservice.dto.response.ErrorResponse;
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

  // 403 Forbidden
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex) {
    return mapToErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null);
  }

  // 404 Not Found
  @ExceptionHandler(NotificationNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTicketNotFoundException(
      NotificationNotFoundException ex) {
    return mapToErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
  }
}
