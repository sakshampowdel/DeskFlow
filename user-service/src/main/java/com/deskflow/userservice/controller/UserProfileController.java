package com.deskflow.userservice.controller;

import com.deskflow.userservice.dto.request.PagedUserRequest;
import com.deskflow.userservice.dto.request.UpdateProfileRequest;
import com.deskflow.userservice.dto.response.InternalUserResponse;
import com.deskflow.userservice.dto.response.PagedUserResponse;
import com.deskflow.userservice.dto.response.UserProfileResponse;
import com.deskflow.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserProfileController {

  private final UserProfileService userProfileService;

  @GetMapping("/me")
  public ResponseEntity<UserProfileResponse> getMyProfile(
      @RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok(userProfileService.getMyProfile(userId));
  }

  @PatchMapping("/me")
  public ResponseEntity<UserProfileResponse> updateMyProfile(
      @RequestHeader("X-User-Id") String userId,
      @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
    return ResponseEntity.ok(userProfileService.updateMyProfile(userId, updateProfileRequest));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserProfileResponse> getUserById(
      @RequestHeader("X-User-Id") String callerId, @PathVariable String userId) {
    return ResponseEntity.ok(userProfileService.getUserById(userId));
  }

  @GetMapping
  public ResponseEntity<PagedUserResponse> getAllUsers(
      @RequestHeader("X-User-Id") String callerId, @Validated PagedUserRequest request) {
    return ResponseEntity.ok(userProfileService.getAllUsers(request));
  }

  @PatchMapping("/{userId}/deactivate")
  public ResponseEntity<Void> deactivateUser(
      @RequestHeader("X-User-Id") String adminId, @PathVariable String userId) {
    userProfileService.deactivateUser(userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/internal/{userId}")
  public ResponseEntity<InternalUserResponse> getInternalUserSummary(@PathVariable String userId) {
    return ResponseEntity.ok(userProfileService.getInternalUserSummary(userId));
  }
}
