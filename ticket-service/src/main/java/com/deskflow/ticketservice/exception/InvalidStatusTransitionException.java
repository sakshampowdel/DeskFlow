package com.deskflow.ticketservice.exception;

public class InvalidStatusTransitionException extends RuntimeException {
  public InvalidStatusTransitionException(String message) {
    super(message);
  }
}
