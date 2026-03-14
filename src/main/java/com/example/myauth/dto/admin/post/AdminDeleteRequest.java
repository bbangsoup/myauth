package com.example.myauth.dto.admin.post;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDeleteRequest {
  @Size(max = 500, message = "reason max length is 500")
  private String reason;
}
