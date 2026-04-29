package com.deskflow.userservice.dto.response;

import java.util.Map;

public record ErrorResponse(int status, String error, String message, Map<String, String> errors) {}
