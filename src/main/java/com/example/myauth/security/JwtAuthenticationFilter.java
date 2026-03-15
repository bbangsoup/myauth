package com.example.myauth.security;

import com.example.myauth.dto.JwtErrorResponse;
import com.example.myauth.entity.User;
import com.example.myauth.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 * 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 인증 정보를 SecurityContext에 설정한다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  /**
   * 모든 HTTP 요청마다 실행되는 필터 메서드
   * JWT 토큰을 검증하고 만료/유효하지 않은 토큰을 구분하여 처리
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    try {
      // Authorization 헤더에서 JWT 토큰 추출
      String token = extractTokenFromRequest(request);

      // 토큰이 존재하면 인증 처리 (예외 발생 시 catch 블록에서 처리)
      if (token != null) {
        authenticateWithToken(token, request);
      }

    } catch (ExpiredJwtException e) {
      // ⚠️ 토큰 만료: 클라이언트가 /refresh를 호출하도록 안내
      log.warn("JWT 토큰 만료 - 사용자: {}, 경로: {}", e.getClaims().getSubject(), request.getRequestURI());
      sendErrorResponse(response, JwtErrorResponse.tokenExpired(request.getRequestURI()), HttpStatus.UNAUTHORIZED);
      return; // 필터 체인 중단

    } catch (JwtException | IllegalArgumentException e) {
      // ❌ 유효하지 않은 토큰: 클라이언트가 재로그인하도록 안내
      log.error("유효하지 않은 JWT 토큰 - 경로: {}, 오류: {}", request.getRequestURI(), e.getMessage());
      sendErrorResponse(response, JwtErrorResponse.invalidToken(request.getRequestURI()), HttpStatus.UNAUTHORIZED);
      return; // 필터 체인 중단

    } catch (Exception e) {
      // 예상치 못한 오류: 로그 기록 후 필터 체인 계속 진행 (인증 실패로 처리)
      log.error("JWT 인증 처리 중 예상치 못한 오류 - 경로: {}, 오류: {}", request.getRequestURI(), e.getMessage(), e);
    }

    // 다음 필터로 요청 전달
    filterChain.doFilter(request, response);
  }

  /**
   * JWT 토큰을 검증하고 인증 정보를 SecurityContext에 설정
   * validateToken() 내부에서 ExpiredJwtException, JwtException 등이 발생하면 상위로 전파됨
   *
   * @param token JWT 토큰 문자열
   * @param request HTTP 요청 객체
   * @throws ExpiredJwtException 토큰이 만료된 경우 (validateToken 내부에서 발생)
   * @throws JwtException 토큰이 유효하지 않은 경우 (validateToken 내부에서 발생)
   */
  private void authenticateWithToken(String token, HttpServletRequest request) {
    // 토큰 검증 (만료 시 ExpiredJwtException, 유효하지 않으면 JwtException 발생)
    jwtTokenProvider.validateToken(token);

    // 토큰에서 사용자 정보 추출
    String email = jwtTokenProvider.getEmailFromToken(token);
    Long userId = jwtTokenProvider.getUserIdFromToken(token);

    log.debug("JWT 토큰 검증 성공 - 이메일: {}, userId: {}", email, userId);

    // DB에서 사용자 조회
    User user = userRepository.findById(userId).orElse(null);

    // 사용자가 존재하고 활성화 상태인 경우에만 인증 설정
    if (user != null && user.getIsActive()) {
      // Spring Security 인증 객체 생성
      List<SimpleGrantedAuthority> authorities = List.of(
          new SimpleGrantedAuthority(resolveAuthority(user))
      );

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(user, null, authorities);

      // 요청 정보 추가 (IP 주소 등)
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      // SecurityContext에 인증 정보 설정
      SecurityContextHolder.getContext().setAuthentication(authentication);

      log.debug("SecurityContext에 인증 정보 설정 완료: {}", email);
    }
  }

  /**
   * HTTP 요청의 Authorization 헤더에서 JWT 토큰 추출
   *
   * @param request HTTP 요청
   * @return JWT 토큰 (없으면 null)
   */
  private String extractTokenFromRequest(HttpServletRequest request) {
    // Authorization 헤더 값 가져오기
    String bearerToken = request.getHeader("Authorization");

    // "Bearer {token}" 형식인지 확인하고 토큰만 추출
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7); // "Bearer " 이후의 토큰 문자열 반환
    }

    return null;
  }

  private String resolveAuthority(User user) {
    if (Boolean.TRUE.equals(user.getIsSuperUser()) || user.getRole() == User.Role.ROLE_ADMIN) {
      return User.Role.ROLE_ADMIN.name();
    }
    return user.getRole().name();
  }

  /**
   * JWT 인증 실패 시 JSON 형식의 에러 응답 전송
   * 클라이언트가 토큰 만료와 유효하지 않은 토큰을 구분할 수 있도록 상세한 정보 제공
   *
   * @param response HTTP 응답 객체
   * @param errorResponse 에러 응답 DTO (errorCode, message, action 포함)
   * @param status HTTP 상태 코드 (401 Unauthorized)
   * @throws IOException JSON 직렬화 또는 응답 작성 중 오류 발생 시
   */
  private void sendErrorResponse(
      HttpServletResponse response,
      JwtErrorResponse errorResponse,
      HttpStatus status
  ) throws IOException {
    // 응답 설정
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // JSON 직렬화 및 응답 작성 (Spring Boot가 자동 제공하는 ObjectMapper 사용)
    String jsonResponse = objectMapper.writeValueAsString(errorResponse);
    response.getWriter().write(jsonResponse);
  }
}
