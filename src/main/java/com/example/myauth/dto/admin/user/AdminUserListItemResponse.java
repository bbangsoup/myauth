package com.example.myauth.dto.admin.user;

import com.example.myauth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminUserListItemResponse {
  private Long id;
  private String email;
  private String name;
  private User.Role role;
  private User.Status status;
  private Boolean isActive;
  private Boolean isSuperUser;
  private LocalDateTime createdAt;
  private LocalDateTime lastLoginAt;

  public static AdminUserListItemResponse from(User user) {
    return AdminUserListItemResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .role(user.getRole())
        .status(user.getStatus())
        .isActive(user.getIsActive())
        .isSuperUser(user.getIsSuperUser())
        .createdAt(user.getCreatedAt())
        .lastLoginAt(user.getLastLoginAt())
        .build();
  }
}
