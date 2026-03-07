package com.example.myauth.controller;

import com.example.myauth.config.AppProperties;
import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.LoginResponse;
import com.example.myauth.dto.kakao.KakaoOAuthDto;
import com.example.myauth.service.KakaoOAuthService;
import com.example.myauth.util.ClientTypeDetector;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 카카오 OAuth 로그인 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/kakao")
@RequiredArgsConstructor
public class KakaoAuthController {

  private final KakaoOAuthService kakaoOAuthService;
  private final AppProperties appProperties;

  /**
   * 토큰 교환 엔드포인트
   * 세션에 저장된 로그인 결과를 꺼내 refresh token 쿠키로 설정한다.
   */
  @PostMapping("/exchange-token")
  public ResponseEntity<ApiResponse<LoginResponse>> exchangeToken(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    log.info("토큰 교환 요청");

    HttpSession session = request.getSession(false);
    if (session == null) {
      log.warn("세션이 없어 토큰 교환 실패");
      return ResponseEntity
          .status(401)
          .body(ApiResponse.error("세션이 만료되었습니다. 다시 로그인해주세요."));
    }

    LoginResponse loginResponse = (LoginResponse) session.getAttribute("pendingLoginResponse");
    if (loginResponse == null) {
      log.warn("세션에 pendingLoginResponse가 없어 토큰 교환 실패");
      return ResponseEntity
          .status(401)
          .body(ApiResponse.error("로그인 정보가 없습니다. 다시 로그인해주세요."));
    }

    session.removeAttribute("pendingLoginResponse");

    ResponseCookie refreshTokenCookie = ResponseCookie
        .from("refreshToken", loginResponse.getRefreshToken())
        .httpOnly(true)
        .secure(appProperties.getCookie().isSecure())
        .path("/")
        .maxAge(7 * 24 * 60 * 60)
        .sameSite("Lax")
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    loginResponse.setRefreshToken(null);

    log.info("토큰 교환 성공 - 사용자: {}", loginResponse.getUser().getEmail());
    return ResponseEntity.ok(ApiResponse.success("토큰 교환 성공", loginResponse));
  }

  /**
   * 카카오 로그인 시작
   */
  @GetMapping("/login")
  public void kakaoLogin(
      @RequestParam(required = false) String redirectUrl,
      HttpSession session,
      HttpServletResponse response
  ) throws IOException {
    log.info("카카오 로그인 요청 - redirectUrl: {}", redirectUrl);

    if (redirectUrl != null && !redirectUrl.isBlank()) {
      session.setAttribute("kakaoRedirectUrl", redirectUrl);
      log.info("프론트 redirectUrl을 세션에 저장: {}", redirectUrl);
    }

    String authorizationUrl = kakaoOAuthService.getAuthorizationUrl();
    log.info("카카오 인증 페이지로 리다이렉트: {}", authorizationUrl);
    response.sendRedirect(authorizationUrl);
  }

  /**
   * 카카오 OAuth 콜백 처리
   */
  @GetMapping("/callback")
  public void kakaoCallback(
      @RequestParam String code,
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {
    log.info("카카오 로그인 콜백 - code 수신");

    try {
      HttpSession session = request.getSession(false);
      String frontendRedirectUrl = null;

      if (session != null) {
        frontendRedirectUrl = (String) session.getAttribute("kakaoRedirectUrl");
        if (frontendRedirectUrl != null) {
          session.removeAttribute("kakaoRedirectUrl");
          log.info("세션에서 redirectUrl 복원: {}", frontendRedirectUrl);
        }
      }

      if (frontendRedirectUrl == null || frontendRedirectUrl.isBlank()) {
        frontendRedirectUrl = appProperties.getOauth().getKakaoRedirectUrl();
        log.info("세션 값이 없어 기본 redirectUrl 사용: {}", frontendRedirectUrl);
      }

      boolean isWebClient = ClientTypeDetector.isWebClient(request);
      String clientType = ClientTypeDetector.getClientTypeString(request);
      log.info("감지된 클라이언트 타입: {}", clientType);

      KakaoOAuthDto.TokenResponse tokenResponse = kakaoOAuthService.getAccessToken(code);
      KakaoOAuthDto.UserInfoResponse kakaoUserInfo = kakaoOAuthService.getUserInfo(tokenResponse.getAccessToken());
      LoginResponse loginResponse = kakaoOAuthService.processKakaoLogin(kakaoUserInfo);

      if (isWebClient) {
        // 새로고침 시 access token 유실을 대비해 refresh token 쿠키를 설정한다.
        ResponseCookie refreshTokenCookie = ResponseCookie
            .from("refreshToken", loginResponse.getRefreshToken())
            .httpOnly(true)
            .secure(appProperties.getCookie().isSecure())
            .path("/")
            .maxAge(7 * 24 * 60 * 60)
            .sameSite("Lax")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        log.info("웹 로그인 refreshToken 쿠키 설정 완료");

        String userJson = String.format(
            "{\"id\":%d,\"email\":\"%s\",\"name\":\"%s\",\"profileImage\":%s}",
            loginResponse.getUser().getId(),
            loginResponse.getUser().getEmail(),
            loginResponse.getUser().getName(),
            loginResponse.getUser().getProfileImage() != null
                ? "\"" + loginResponse.getUser().getProfileImage() + "\""
                : "null"
        );
        String encodedUser = java.net.URLEncoder.encode(userJson, "UTF-8");

        String successRedirectUrl = String.format(
            "%s#accessToken=%s&user=%s",
            frontendRedirectUrl,
            loginResponse.getAccessToken(),
            encodedUser
        );

        log.info("프론트로 리다이렉트: {}", frontendRedirectUrl);
        response.sendRedirect(successRedirectUrl);
      } else {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
            "{\"success\":true,\"message\":\"카카오 로그인 성공\",\"data\":{\"accessToken\":\"%s\",\"refreshToken\":\"%s\",\"user\":{\"id\":%d,\"email\":\"%s\",\"name\":\"%s\"}}}",
            loginResponse.getAccessToken(),
            loginResponse.getRefreshToken(),
            loginResponse.getUser().getId(),
            loginResponse.getUser().getEmail(),
            loginResponse.getUser().getName()
        );
        response.getWriter().write(jsonResponse);
      }

      log.info("카카오 로그인 성공: {}, 클라이언트: {}", loginResponse.getUser().getEmail(), clientType);

    } catch (Exception e) {
      log.error("카카오 로그인 실패: {}", e.getMessage(), e);

      HttpSession session = request.getSession(false);
      String errorRedirectUrl = null;

      if (session != null) {
        errorRedirectUrl = (String) session.getAttribute("kakaoRedirectUrl");
        if (errorRedirectUrl != null) {
          session.removeAttribute("kakaoRedirectUrl");
        }
      }

      if (errorRedirectUrl == null || errorRedirectUrl.isBlank()) {
        errorRedirectUrl = appProperties.getOauth().getKakaoRedirectUrl();
      }

      String finalErrorRedirectUrl = String.format(
          "%s?error=%s",
          errorRedirectUrl,
          java.net.URLEncoder.encode(e.getMessage(), "UTF-8")
      );
      response.sendRedirect(finalErrorRedirectUrl);
    }
  }
}