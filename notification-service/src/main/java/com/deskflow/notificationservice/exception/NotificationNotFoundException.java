package com.deskflow.notificationservice.exception;

public class NotificationNotFoundException extends RuntimeException {
  public NotificationNotFoundException(String message) {
    super(message);
  }
}
