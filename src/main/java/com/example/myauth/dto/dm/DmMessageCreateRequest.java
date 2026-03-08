package com.example.myauth.dto.dm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmMessageCreateRequest {

  @NotBlank(message = "content는 필수입니다.")
  @Size(max = 2000, message = "content는 2000자를 넘길 수 없습니다.")
  private String content;
}
