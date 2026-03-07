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
 * 燁삳똻萸??OAuth 嚥≪뮄????뚢뫂?껅에?살쑎
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/kakao")
@RequiredArgsConstructor
public class KakaoAuthController {

  private final KakaoOAuthService kakaoOAuthService;
  private final AppProperties appProperties;

  /**
   * ?醫뤾쿃 ?대????遺얜굡?????   * ?紐꾨?????貫留?嚥≪뮄???野껉퀗?든몴?揶쎛?紐? refresh token ?묒쥚沅롧몴???쇱젟??뺣뼄.
   */
  @PostMapping("/exchange-token")
  public ResponseEntity<ApiResponse<LoginResponse>> exchangeToken(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    log.info("?醫뤾쿃 ?대????遺욧퍕");

    HttpSession session = request.getSession(false);
    if (session == null) {
      log.warn("?紐꾨????곷선 ?醫뤾쿃 ?대?????쎈솭");
      return ResponseEntity
          .status(401)
          .body(ApiResponse.error("?紐꾨??筌띾슢利??뤿???щ빍?? ??쇰뻻 嚥≪뮄??紐낅퉸雅뚯눘苑??"));
    }

    LoginResponse loginResponse = (LoginResponse) session.getAttribute("pendingLoginResponse");
    if (loginResponse == null) {
      log.warn("?紐꾨??pendingLoginResponse揶쎛 ??곷선 ?醫뤾쿃 ?대?????쎈솭");
      return ResponseEntity
          .status(401)
          .body(ApiResponse.error("嚥≪뮄????類ｋ궖揶쎛 ??곷뮸??덈뼄. ??쇰뻻 嚥≪뮄??紐낅퉸雅뚯눘苑??"));
    }

    session.removeAttribute("pendingLoginResponse");

    ResponseCookie refreshTokenCookie = ResponseCookie
        .from("refreshToken", loginResponse.getRefreshToken())
        .httpOnly(true)
        .secure(appProperties.getCookie().isSecure())
        .path("/")
        .maxAge(7 * 24 * 60 * 60)
        .sameSite("Lax").build();

    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    loginResponse.setRefreshToken(null);

    log.info("?醫뤾쿃 ?대????源껊궗 - ????? {}", loginResponse.getUser().getEmail());
    return ResponseEntity.ok(ApiResponse.success("?醫뤾쿃 ?대????源껊궗", loginResponse));
  }

  /**
   * 燁삳똻萸??嚥≪뮄?????뽰삂
   */
  @GetMapping("/login")
  public void kakaoLogin(
      @RequestParam(required = false) String redirectUrl,
      HttpSession session,
      HttpServletResponse response
  ) throws IOException {
    log.info("燁삳똻萸??嚥≪뮄????遺욧퍕 - redirectUrl: {}", redirectUrl);

    if (redirectUrl != null && !redirectUrl.isBlank()) {
      session.setAttribute("kakaoRedirectUrl", redirectUrl);
      log.info("?袁⑥쨴??redirectUrl???紐꾨?????? {}", redirectUrl);
    }

    String authorizationUrl = kakaoOAuthService.getAuthorizationUrl();
    log.info("燁삳똻萸???紐꾩쵄 ??륁뵠筌왖嚥??귐됰뼄????? {}", authorizationUrl);
    response.sendRedirect(authorizationUrl);
  }

  /**
   * 燁삳똻萸??OAuth ?꾩뮆媛?筌ｌ꼶??   */
  @GetMapping("/callback")
  public void kakaoCallback(
      @RequestParam String code,
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {
    log.info("燁삳똻萸??嚥≪뮄????꾩뮆媛?- code ??뤿뻿");

    try {
      HttpSession session = request.getSession(false);
      String frontendRedirectUrl = null;

      if (session != null) {
        frontendRedirectUrl = (String) session.getAttribute("kakaoRedirectUrl");
        if (frontendRedirectUrl != null) {
          session.removeAttribute("kakaoRedirectUrl");
          log.info("?紐꾨?癒?퐣 redirectUrl 癰귣벊?? {}", frontendRedirectUrl);
        }
      }

      if (frontendRedirectUrl == null || frontendRedirectUrl.isBlank()) {
        frontendRedirectUrl = appProperties.getOauth().getKakaoRedirectUrl();
        log.info("?紐꾨?揶쏅?????곷선 疫꿸퀡??redirectUrl ???? {}", frontendRedirectUrl);
      }

      boolean isWebClient = ClientTypeDetector.isWebClient(request);
      String clientType = ClientTypeDetector.getClientTypeString(request);
      log.info("揶쏅Ŋ????????곷섧?????? {}", clientType);

      KakaoOAuthDto.TokenResponse tokenResponse = kakaoOAuthService.getAccessToken(code);
      KakaoOAuthDto.UserInfoResponse kakaoUserInfo = kakaoOAuthService.getUserInfo(tokenResponse.getAccessToken());
      LoginResponse loginResponse = kakaoOAuthService.processKakaoLogin(kakaoUserInfo);

      if (isWebClient) {
        ResponseCookie refreshTokenCookie = ResponseCookie
            .from("refreshToken", loginResponse.getRefreshToken())
            .httpOnly(true)
            .secure(appProperties.getCookie().isSecure())
            .path("/")
            .maxAge(7 * 24 * 60 * 60)
            .sameSite("Lax").build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        log.info("??嚥≪뮄???refreshToken ?묒쥚沅???쇱젟 ?袁⑥┷");

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

        log.info("?袁⑥쨴?紐껋쨮 ?귐됰뼄????? {}", frontendRedirectUrl);
        response.sendRedirect(successRedirectUrl);
      } else {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
            "{\"success\":true,\"message\":\"燁삳똻萸??嚥≪뮄????源껊궗\",\"data\":{\"accessToken\":\"%s\",\"refreshToken\":\"%s\",\"user\":{\"id\":%d,\"email\":\"%s\",\"name\":\"%s\"}}}",
            loginResponse.getAccessToken(),
            loginResponse.getRefreshToken(),
            loginResponse.getUser().getId(),
            loginResponse.getUser().getEmail(),
            loginResponse.getUser().getName()
        );
        response.getWriter().write(jsonResponse);
      }

      log.info("燁삳똻萸??嚥≪뮄????源껊궗: {}, ?????곷섧?? {}", loginResponse.getUser().getEmail(), clientType);

    } catch (Exception e) {
      log.error("燁삳똻萸??嚥≪뮄?????쎈솭: {}", e.getMessage(), e);

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


