package com.deskflow.userservice.exception;

import com.deskflow.userservice.dto.response.ErrorResponse;
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

  // 404 Not Found
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
    return mapToErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
  }
}
