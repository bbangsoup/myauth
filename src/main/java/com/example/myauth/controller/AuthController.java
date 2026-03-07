package com.example.myauth.controller;

import com.example.myauth.config.AppProperties;
import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.LoginRequest;
import com.example.myauth.dto.LoginResponse;
import com.example.myauth.dto.SignupRequest;
import com.example.myauth.dto.TokenRefreshRequest;
import com.example.myauth.dto.TokenRefreshResponse;
import com.example.myauth.service.AuthService;
import com.example.myauth.util.ClientTypeDetector;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final AppProperties appProperties;

  @GetMapping("/health")
  public ResponseEntity<ApiResponse<Void>> health() {
    return ResponseEntity.ok(ApiResponse.success("Auth Service is running...."));
  }

  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest signupRequest) {
    log.info("회원가입 요청: {}", signupRequest.getEmail());
    authService.registerUser(signupRequest);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("회원가입이 완료되었습니다."));
  }

  @PostMapping("/old_login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
    log.info("로그인 요청: {}", loginRequest.getEmail());
    LoginResponse loginResponse = authService.login(loginRequest);
    return ResponseEntity.ok(ApiResponse.success("로그인 성공", loginResponse));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> loginEx(
      @Valid @RequestBody LoginRequest loginRequest,
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    log.info("로그인 요청 (loginEx): {}", loginRequest.getEmail());

    boolean isWebClient = ClientTypeDetector.isWebClient(request);
    String clientType = ClientTypeDetector.getClientTypeString(request);
    log.info("감지된 클라이언트 타입: {}", clientType);
    ClientTypeDetector.logUserAgent(request);

    LoginResponse loginResponse = authService.loginEx(loginRequest);

    if (isWebClient) {
      log.info("웹 클라이언트 감지 - Refresh Token을 HTTP-only 쿠키로 설정");

      ResponseCookie refreshTokenCookie = ResponseCookie
          .from("refreshToken", loginResponse.getRefreshToken())
          .httpOnly(true)
          .secure(appProperties.getCookie().isSecure())
          .path("/")
          .maxAge(7 * 24 * 60 * 60)
          .sameSite("Lax")
          .build();

      response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
      log.info("Refresh Token 쿠키 설정 완료");

      loginResponse.setRefreshToken(null);
      log.info("응답 바디에서 Refresh Token 제거");
    } else {
      log.info("모바일 클라이언트 감지 - Refresh Token을 JSON 응답에 포함");
    }

    log.info("로그인 성공 (loginEx): {}, 클라이언트: {}", loginRequest.getEmail(), clientType);
    return ResponseEntity.ok(ApiResponse.success("로그인 성공", loginResponse));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
      HttpServletRequest request,
      @RequestBody(required = false) @Valid TokenRefreshRequest body
  ) {
    log.info("Access Token 갱신 요청");

    boolean isWebClient = ClientTypeDetector.isWebClient(request);
    String clientType = ClientTypeDetector.getClientTypeString(request);
    log.info("클라이언트 타입: {}", clientType);

    String refreshToken;
    if (isWebClient) {
      log.info("웹 클라이언트 - 쿠키에서 Refresh Token 추출");
      refreshToken = extractRefreshTokenFromCookies(request);

      if (refreshToken == null) {
        log.warn("쿠키에 Refresh Token이 없음");
        refreshToken = extractRefreshTokenFromBody(body);
        if (refreshToken != null) {
          log.info("테스트/예외 상황 - 웹 클라이언트지만 요청 바디의 Refresh Token 사용");
        } else {
          return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ApiResponse.error("Refresh Token이 없습니다. 다시 로그인해주세요."));
        }
      }
    } else {
      log.info("모바일 클라이언트 - 요청 바디에서 Refresh Token 추출");
      refreshToken = extractRefreshTokenFromBody(body);

      if (refreshToken == null) {
        log.warn("요청 바디에 Refresh Token이 없음");
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Refresh Token은 필수입니다."));
      }
    }

    TokenRefreshResponse refreshResponse = authService.refreshAccessToken(refreshToken);
    log.info("Access Token 갱신 성공");
    return ResponseEntity.ok(ApiResponse.success("Access Token refreshed", refreshResponse));
  }

  private String extractRefreshTokenFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if ("refreshToken".equals(cookie.getName())) {
        log.debug("쿠키에서 Refresh Token 발견");
        return cookie.getValue();
      }
    }

    return null;
  }

  private String extractRefreshTokenFromBody(TokenRefreshRequest body) {
    if (body == null || body.getRefreshToken() == null || body.getRefreshToken().isBlank()) {
      return null;
    }

    log.debug("요청 바디에서 Refresh Token 발견");
    return body.getRefreshToken();
  }
}