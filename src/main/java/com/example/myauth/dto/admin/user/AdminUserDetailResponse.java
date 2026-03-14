package com.example.myauth.dto.admin.user;

import com.example.myauth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminUserDetailResponse {
  private Long id;
  private String email;
  private String name;
  private User.Role role;
  private User.Status status;
  private Boolean isActive;
  private Boolean isSuperUser;
  private String provider;
  private String providerId;
  private String profileImage;
  private Integer failedLoginAttempts;
  private LocalDateTime accountLockedUntil;
  private String lastLoginIp;
  private LocalDateTime lastLoginAt;
  private LocalDateTime emailVerifiedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static AdminUserDetailResponse from(User user) {
    return AdminUserDetailResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .role(user.getRole())
        .status(user.getStatus())
        .isActive(user.getIsActive())
        .isSuperUser(user.getIsSuperUser())
        .provider(user.getProvider())
        .providerId(user.getProviderId())
        .profileImage(user.getProfileImage())
        .failedLoginAttempts(user.getFailedLoginAttempts())
        .accountLockedUntil(user.getAccountLockedUntil())
        .lastLoginIp(user.getLastLoginIp())
        .lastLoginAt(user.getLastLoginAt())
        .emailVerifiedAt(user.getEmailVerifiedAt())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
