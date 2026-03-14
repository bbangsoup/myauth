package com.example.myauth.dto.admin.user;

import com.example.myauth.entity.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserRoleUpdateRequest {

  @NotNull(message = "role is required")
  private User.Role role;

  private Boolean isSuperUser;

  @Size(max = 500, message = "reason max length is 500")
  private String reason;
}
