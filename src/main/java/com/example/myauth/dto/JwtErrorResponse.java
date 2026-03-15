package com.example.myauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtErrorResponse {
  private String errorCode;
  private String message;
  private String action;
  private String path;

  public static JwtErrorResponse tokenExpired(String path) {
    return JwtErrorResponse.builder()
        .errorCode("TOKEN_EXPIRED")
        .message("Access Token이 만료되었습니다. 토큰을 갱신해주세요.")
        .action("REFRESH_TOKEN")
        .path(path)
        .build();
  }

  public static JwtErrorResponse invalidToken(String path) {
    return JwtErrorResponse.builder()
        .errorCode("INVALID_TOKEN")
        .message("유효하지 않은 토큰입니다. 다시 로그인해주세요.")
        .action("LOGIN_REQUIRED")
        .path(path)
        .build();
  }

  public static JwtErrorResponse noToken(String path) {
    return JwtErrorResponse.builder()
        .errorCode("NO_TOKEN")
        .message("인증이 필요합니다. 로그인해주세요.")
        .action("LOGIN_REQUIRED")
        .path(path)
        .build();
  }
}
