package com.deskflow.authservice.exception;

import java.time.LocalDateTime;
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

  private ResponseEntity<Map<String, Object>> mapToErrorResponse(
      HttpStatus httpStatus, String error, String message, Map<String, String> errors) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", httpStatus.value());
    body.put("error", error);
    body.put("message", message);

    if (errors != null) {
      body.put("errors", errors);
    }

    return new ResponseEntity<>(body, httpStatus);
  }

  // Generic Handler
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
    return mapToErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        "An unexpected error occurred",
        null);
  }

  // 400 Bad Request
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
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

    return mapToErrorResponse(
        HttpStatus.BAD_REQUEST,
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "Validation failed",
        errors);
  }

  // 401 Unauthorized
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidCredentialsException(
      InvalidCredentialsException ex) {
    return mapToErrorResponse(
        HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage(), null);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidTokenException(
      InvalidCredentialsException ex) {
    return mapToErrorResponse(
        HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage(), null);
  }

  @ExceptionHandler(TokenExpiredException.class)
  public ResponseEntity<Map<String, Object>> handleTokenExpiredException(TokenExpiredException ex) {
    return mapToErrorResponse(
        HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage(), null);
  }

  // 404 Not Found
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
    return mapToErrorResponse(
        HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), null);
  }

  // 409 Conflict
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<Map<String, Object>> handleEmailAlreadyExistsException(
      EmailAlreadyExistsException ex) {
    return mapToErrorResponse(
        HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), null);
  }
}
