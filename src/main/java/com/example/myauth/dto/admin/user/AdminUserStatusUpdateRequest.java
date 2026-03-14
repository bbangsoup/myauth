package com.example.myauth.dto.admin.user;

import com.example.myauth.entity.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserStatusUpdateRequest {

  @NotNull(message = "status is required")
  private User.Status status;

  @Min(value = 1, message = "suspendDays must be >= 1")
  private Integer suspendDays;

  @Size(max = 500, message = "reason max length is 500")
  private String reason;
}
