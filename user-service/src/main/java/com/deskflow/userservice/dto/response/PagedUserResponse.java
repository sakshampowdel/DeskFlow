package com.deskflow.userservice.dto.response;

import java.util.List;

public record PagedUserResponse(
    List<UserProfileResponse> content, int page, int size, long totalElements, int totalPages) {}
