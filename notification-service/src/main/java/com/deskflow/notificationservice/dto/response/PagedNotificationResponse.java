package com.deskflow.notificationservice.dto.response;

import java.util.List;

public record PagedNotificationResponse(
    List<NotificationResponse> notifications,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last) {}
