package com.deskflow.authservice.exception;

import com.deskflow.authservice.dto.reponse.ErrorResponse;
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

  // 401 Unauthorized
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
      InvalidCredentialsException ex) {
    return mapToErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
    return mapToErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
  }

  @ExceptionHandler(TokenExpiredException.class)
  public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException ex) {
    return mapToErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
  }

  // 404 Not Found
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
    return mapToErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
  }

  // 409 Conflict
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
      EmailAlreadyExistsException ex) {
    return mapToErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
  }
}
