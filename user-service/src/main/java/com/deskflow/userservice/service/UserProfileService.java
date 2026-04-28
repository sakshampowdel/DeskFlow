package com.deskflow.userservice.service;

import com.deskflow.userservice.dto.request.PagedUserRequest;
import com.deskflow.userservice.dto.request.UpdateProfileRequest;
import com.deskflow.userservice.dto.response.InternalUserResponse;
import com.deskflow.userservice.dto.response.PagedUserResponse;
import com.deskflow.userservice.dto.response.UserProfileResponse;
import com.deskflow.userservice.exception.UserNotFoundException;
import com.deskflow.userservice.model.UserProfile;
import com.deskflow.userservice.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

  private final UserProfileRepository userProfileRepository;

  private UserProfile findById(String userId) {
    return userProfileRepository
        .findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
  }

  private UserProfileResponse mapToUserProfileResponse(UserProfile profile) {
    return new UserProfileResponse(
        profile.getId(),
        profile.getEmail(),
        profile.getFullName(),
        profile.getRole(),
        profile.getDepartment(),
        profile.getAvatarUrl(),
        profile.isActive(),
        profile.getCreatedAt(),
        profile.getUpdatedAt());
  }

  public UserProfileResponse getMyProfile(String userId) {
    UserProfile profile = findById(userId);
    return mapToUserProfileResponse(profile);
  }

  public UserProfileResponse updateMyProfile(String userId, UpdateProfileRequest request) {
    UserProfile profile = findById(userId);
    if (request.fullName() != null) profile.setFullName(request.fullName());
    if (request.department() != null) profile.setDepartment(request.department());
    if (request.avatarUrl() != null) profile.setAvatarUrl(request.avatarUrl());
    return mapToUserProfileResponse(userProfileRepository.save(profile));
  }

  public UserProfileResponse getUserById(String userId) {
    return mapToUserProfileResponse(findById(userId));
  }

  public PagedUserResponse getAllUsers(PagedUserRequest request) {
    Page<UserProfile> page =
        userProfileRepository.findAll(PageRequest.of(request.page(), request.size()));
    return new PagedUserResponse(
        page.getContent().stream().map(this::mapToUserProfileResponse).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }

  public void deactivateUser(String userId) {
    UserProfile profile = findById(userId);
    profile.setActive(false);
    userProfileRepository.save(profile);
  }

  public InternalUserResponse getInternalUserSummary(String userId) {
    UserProfile profile = findById(userId);
    return new InternalUserResponse(profile.getFullName(), profile.getEmail());
  }
}
