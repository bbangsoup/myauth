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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")  // 筌뤴뫀諭?API ?遺얜굡????紐꾨퓠 /api ?臾먮あ???곕떽?
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final AppProperties appProperties;

  @GetMapping("/health")
  public ResponseEntity<ApiResponse<Void>> health() {
    return ResponseEntity.ok(ApiResponse.success("Auth Service is running...."));
  }


  /**
   * ???뜚揶쎛??
   * ?源껊궗 ??201 Created 獄쏆꼹??
   * ??쎈솭 ????됱뇚 獄쏆뮇源?(GlobalExceptionHandler?癒?퐣 筌ｌ꼶??
   */
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest signupRequest) {
    log.info("??쇱벉 ??李??곗쨮 ???뜚揶쎛???遺욧퍕: {}", signupRequest.getEmail());

    // ???뜚揶쎛??筌ｌ꼶??(??쎈솭 ????됱뇚 ??륁춾)
    authService.registerUser(signupRequest);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("???뜚揶쎛??놁뵠 ?袁⑥┷??뤿???щ빍??"));
  }


  /**
   * 嚥≪뮄???
   * ?源껊궗 ??200 OK?? ??ｍ뜞 ?醫뤾쿃 ?類ｋ궖 獄쏆꼹??
   * ??쎈솭 ????됱뇚 獄쏆뮇源?(GlobalExceptionHandler?癒?퐣 筌ｌ꼶??
   */
  @PostMapping("/old_login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
    log.info("嚥≪뮄????遺욧퍕: {}", loginRequest.getEmail());

    // 嚥≪뮄???筌ｌ꼶??(??쎈솭 ????됱뇚 ??륁춾)
    LoginResponse loginResponse = authService.login(loginRequest);

    return ResponseEntity.ok(ApiResponse.success("嚥≪뮄????源껊궗", loginResponse));
  }


  /**
   * 嚥≪뮄???(??륁뵠?됰슢???獄쎻뫗??- ??筌뤴뫀而???닌됲뀋)
   *
   *  Spring Security ??? 獄쎻뫗???곗쨮 嚥≪뮄???筌ｌ꼶??
   * ?????곷섧??????녿퓠 ?怨뺤뵬 ?醫뤾쿃 ?袁⑸꽊 獄쎻뫗?????삘뀮野?筌ｌ꼶??
   * - ???됰슢??怨?: Refresh Token??HTTP-only ?묒쥚沅롦에??袁⑸꽊 (XSS 獄쎻뫗堉?
   * - 筌뤴뫀而???? 筌뤴뫀諭??醫뤾쿃??JSON ?臾먮뼗 獄쏅뗀逾믤에??袁⑸꽊
   *
   * ?源껊궗 ??200 OK?? ??ｍ뜞 ?醫뤾쿃 ?類ｋ궖 獄쏆꼹??
   * ??쎈솭 ????됱뇚 獄쏆뮇源?(GlobalExceptionHandler?癒?퐣 筌ｌ꼶??
   */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> loginEx(
      @Valid @RequestBody LoginRequest loginRequest,
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    log.info("嚥≪뮄????遺욧퍕 (loginEx): {}", loginRequest.getEmail());

    // 1?るㅄ源??????곷섧??????揶쏅Ŋ?
    boolean isWebClient = ClientTypeDetector.isWebClient(request);
    String clientType = ClientTypeDetector.getClientTypeString(request);
    log.info("揶쏅Ŋ????????곷섧?????? {}", clientType);

    // ?遺얠쒔繹? User-Agent ?類ｋ궖 嚥≪뮄??
    ClientTypeDetector.logUserAgent(request);

    // 2?るㅄ源?嚥≪뮄???筌ｌ꼶??(??쎈솭 ????됱뇚 ??륁춾)
    LoginResponse loginResponse = authService.loginEx(loginRequest);

    // 3?るㅄ源????????곷섧?紐껁늺 Refresh Token???묒쥚沅롦에???쇱젟
    if (isWebClient) {
      log.info("???????곷섧??揶쏅Ŋ? ??Refresh Token??HTTP-only ?묒쥚沅롦에???쇱젟");

      // Refresh Token??HTTP-only ?묒쥚沅롦에???쇱젟
      // ResponseCookie???????뤿연 SameSite?? Domain ??욧쉐 筌뤿굞??
      // - SameSite=Lax: CSRF 獄쎻뫗堉?+ ??곗뺘?怨몄뵥 ??????揶쎛??
      // - Domain=localhost: ?????얜떯???띿쓺 筌뤴뫀諭?localhost?癒?퐣 ?묒쥚沅??⑤벊? (localhost:5173??localhost:9080 筌뤴뫀紐??臾롫젏 揶쎛??
      ResponseCookie refreshTokenCookie = ResponseCookie
          .from("refreshToken", loginResponse.getRefreshToken())
          .httpOnly(true)   // JavaScript ?臾롫젏 ?븍뜃? (XSS 獄쎻뫗堉?
          .secure(appProperties.getCookie().isSecure())  // ??띻펾癰???덉읅 ??쇱젟 (揶쏆뮆而? false, ?袁⑥쨮?類ㅻ? true)
          .path("/")        // 筌뤴뫀諭?野껋럥以?癒?퐣 ?묒쥚沅??袁⑸꽊
          .maxAge(7 * 24 * 60 * 60)  // 7??(????μ맄)
          .sameSite("Lax")  // CSRF 獄쎻뫗堉?+ ??곗뺘 ??삵돩野껊슣???뤿퓠???묒쥚沅??袁⑸꽊 ??됱뒠
          
          .build();

      log.info("?묒쥚沅???쇱젟: HttpOnly=true, Secure={}, Path=/, MaxAge=7?? SameSite=Lax, Domain=localhost",
          appProperties.getCookie().isSecure());

      response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
      log.info("Refresh Token???묒쥚沅????쇱젟 ?袁⑥┷");

      // ?臾먮뼗 獄쏅뗀逾?癒?퐣 Refresh Token ??볤탢 (?묒쥚沅롦에??袁⑸꽊??됱몵沃샕嚥?
      loginResponse.setRefreshToken(null);
      log.info("?臾먮뼗 獄쏅뗀逾?癒?퐣 Refresh Token ??볤탢 (癰귣똻釉?揶쏅벤??");
    } else {
      // 4?るㅄ源?筌뤴뫀而???????곷섧?紐껁늺 Refresh Token??JSON ?臾먮뼗????釉?
      log.info("筌뤴뫀而???????곷섧??揶쏅Ŋ? ??Refresh Token??JSON ?臾먮뼗????釉?);
    }

    // 5?るㅄ源?嚥≪뮄????源껊궗 ?臾먮뼗 獄쏆꼹??
    log.info("嚥≪뮄????源껊궗 (loginEx): {}, ?????곷섧?? {}", loginRequest.getEmail(), clientType);
    return ResponseEntity.ok(ApiResponse.success("嚥≪뮄????源껊궗", loginResponse));
  }


  /**
   * Access Token 揶쏄퉮??(??륁뵠?됰슢???獄쎻뫗??- ??筌뤴뫀而???닌됲뀋)
   * Refresh Token??곗쨮 ??덉쨮??Access Token??獄쏆뮄?믦쳸?낅뮉??
   * ?????곷섧??????녿퓠 ?怨뺤뵬 Refresh Token????삘뀲 ?⑤끃肉????덈뮉??
   * - ???됰슢??怨?: HTTP-only ?묒쥚沅?癒?퐣 Refresh Token ??꾨┛
   * - 筌뤴뫀而???? ?遺욧퍕 獄쏅뗀逾?癒?퐣 Refresh Token ??꾨┛
   *
   * ?源껊궗 ??200 OK?? ??ｍ뜞 ??Access Token 獄쏆꼹??
   * ??쎈솭 ????됱뇚 獄쏆뮇源?(GlobalExceptionHandler?癒?퐣 筌ｌ꼶??
   */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
      HttpServletRequest request,
      @RequestBody(required = false) @Valid TokenRefreshRequest body
  ) {
    log.info("Access Token 揶쏄퉮???遺욧퍕");

    // 1?るㅄ源??????곷섧??????揶쏅Ŋ?
    boolean isWebClient = ClientTypeDetector.isWebClient(request);
    String clientType = ClientTypeDetector.getClientTypeString(request);
    log.info("?????곷섧?????? {}", clientType);

    // 2?るㅄ源??????곷섧??????녿퓠 ?怨뺤뵬 Refresh Token ?곕뗄??
    String refreshToken;
    if (isWebClient) {
      log.info("???????곷섧?????묒쥚沅?癒?퐣 Refresh Token ??꾨┛");
      refreshToken = extractRefreshTokenFromCookies(request);

      // ?묒쥚沅???醫뤾쿃????용뮉 野껋럩?? ?遺욧퍕 獄쏅뗀逾?癒?퐣 ??뺣즲 (???뮞???대Ŋ???
      if (refreshToken == null) {
        log.warn("?묒쥚沅??Refresh Token????곸벉");

        // ???뮞???대Ŋ??? ?遺욧퍕 獄쏅뗀逾?癒?퐣 Refresh Token ??꾨┛ ??뺣즲
        refreshToken = extractRefreshTokenFromBody(body);
        if (refreshToken != null) {
          log.info("???뮞???대Ŋ??? ???????곷섧?紐꾩뵠筌왖筌??遺욧퍕 獄쏅뗀逾?癒?퐣 Refresh Token ????);
        } else {
          return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ApiResponse.error("Refresh Token????곷뮸??덈뼄. ??쇰뻻 嚥≪뮄??紐낅퉸雅뚯눘苑??"));
        }
      }
    } else {
      log.info("筌뤴뫀而???????곷섧?????遺욧퍕 獄쏅뗀逾?癒?퐣 Refresh Token ??꾨┛");
      refreshToken = extractRefreshTokenFromBody(body);

      // ?遺욧퍕 獄쏅뗀逾???醫뤾쿃????용뮉 野껋럩???癒?쑎 ?臾먮뼗
      if (refreshToken == null) {
        log.warn("?遺욧퍕 獄쏅뗀逾??Refresh Token????곸벉");
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Refresh Token?? ?袁⑸땾??낅빍??"));
      }
    }

    // 3?るㅄ源?Refresh Token??곗쨮 ??Access Token 獄쏆뮄??(??쎈솭 ????됱뇚 ??륁춾)
    TokenRefreshResponse refreshResponse = authService.refreshAccessToken(refreshToken);

    // 4?るㅄ源??臾먮뼗 獄쏆꼹??
    log.info("Access Token 揶쏄퉮???源껊궗");
    return ResponseEntity.ok(ApiResponse.success("Access Token refreshed", refreshResponse));
  }

  /**
   * HTTP ?묒쥚沅?癒?퐣 Refresh Token???곕뗄???뺣뼄
   *
   * @param request HTTP ?遺욧퍕 揶쏆빘猿?
   * @return Refresh Token (??곸몵筌?null)
   */
  private String extractRefreshTokenFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if ("refreshToken".equals(cookie.getName())) {
        log.debug("?묒쥚沅?癒?퐣 Refresh Token 獄쏆뮄猿?);
        return cookie.getValue();
      }
    }

    return null;
  }

  /**
   * ?遺욧퍕 獄쏅뗀逾?癒?퐣 Refresh Token???곕뗄???뺣뼄
   *
   * @param body ?醫뤾쿃 揶쏄퉮???遺욧퍕 獄쏅뗀逾?
   * @return Refresh Token (??얘탢???醫륁뒞??? ??놁몵筌?null)
   */
  private String extractRefreshTokenFromBody(TokenRefreshRequest body) {
    if (body == null || body.getRefreshToken() == null || body.getRefreshToken().isBlank()) {
      return null;
    }

    log.debug("?遺욧퍕 獄쏅뗀逾?癒?퐣 Refresh Token 獄쏆뮄猿?);
    return body.getRefreshToken();
  }
}
