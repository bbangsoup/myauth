package com.example.myauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token Refresh 응답 DTO
 * ApiResponse의 data 필드에 담겨서 반환됨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshResponse {

  /**
   * 새로 발급된 Access Token
   */
  private String accessToken;

  /**
   * 새로 발급된 Refresh Token (선택적)
   * Refresh Token Rotation을 사용하는 경우에만 포함됨
   * 현재는 사용하지 않으므로 null
   */
  private String refreshToken;

  /**
   * 현재 사용자 정보
   */
  private LoginResponse.UserInfo user;
}
