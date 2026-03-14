package com.example.myauth.config;

import com.example.myauth.security.CustomLogoutHandler;
import com.example.myauth.security.CustomLogoutSuccessHandler;
import com.example.myauth.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security ?ㅼ젙
 * - JWT 湲곕컲 ?몄쬆 ?ъ슜 (?몄뀡 ?ъ슜 ????
 * - 寃쎈줈蹂??몄쬆 洹쒖튃 ?ㅼ젙
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomLogoutHandler customLogoutHandler;
  private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

  /**
   * 鍮꾨?踰덊샇 ?뷀샇?붿뿉 ?ъ슜??PasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Spring Security???쒖? ?몄쬆 愿由ъ옄
   * ?ъ슜???몄쬆??泥섎━?섎뒗 ?듭떖 而댄룷?뚰듃
   * - UserDetailsService瑜??듯빐 ?ъ슜???뺣낫 濡쒕뱶
   * - PasswordEncoder瑜??듯빐 鍮꾨?踰덊샇 寃利?
   * - 怨꾩젙 ?곹깭 ?뺤씤 (?쒖꽦?? ?좉툑, 留뚮즺 ??
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Spring Security ?꾪꽣 泥댁씤 ?ㅼ젙
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // 1截뤴깵 CSRF 鍮꾪솢?깊솕 (JWT ?ъ슜 ??遺덊븘??
        .csrf(AbstractHttpConfigurer::disable)

        // 2截뤴깵 ??濡쒓렇??鍮꾪솢?깊솕
        .formLogin(AbstractHttpConfigurer::disable)

        // 3截뤴깵 HTTP Basic ?몄쬆 鍮꾪솢?깊솕
        .httpBasic(AbstractHttpConfigurer::disable)

        // 4截뤴깵 ?몄뀡 ?ъ슜 ????(JWT ?ъ슜)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // 5截뤴깵 濡쒓렇?꾩썐 ?ㅼ젙
        .logout(logout -> logout
            .logoutUrl("/logout")  // 濡쒓렇?꾩썐 URL
            .addLogoutHandler(customLogoutHandler)  // 而ㅼ뒪? 濡쒓렇?꾩썐 ?몃뱾??(Refresh Token ??젣)
            .logoutSuccessHandler(customLogoutSuccessHandler)  // 而ㅼ뒪? ?깃났 ?몃뱾??(JSON ?묐떟)
            .permitAll()  // 濡쒓렇?꾩썐 URL? ?몄쬆 ?놁씠 ?묎렐 媛??
        )

        // 6截뤴깵 寃쎈줈蹂??몄쬆 洹쒖튃 ?ㅼ젙
        .authorizeHttpRequests(auth ->
            auth
                // ?몄쬆 ?놁씠 ?묎렐 媛?ν븳 寃쎈줈
                .requestMatchers("/health", "/signup", "/login", "/loginEx", "/refresh").permitAll()
                // /api ?묐몢?ш? 遺숈? 寃쎈줈???덉슜 (?꾨줎?몄뿏???명솚??
                .requestMatchers("/api/health", "/api/signup", "/api/login", "/api/loginEx", "/api/refresh").permitAll()
                // 移댁뭅??OAuth 濡쒓렇??寃쎈줈 (?몄쬆 遺덊븘??
                .requestMatchers("/auth/kakao/**", "/api/auth/kakao/**").permitAll()
                // ?낅줈?쒕맂 ?대?吏 ?뚯씪 ?묎렐 (?몄쬆 遺덊븘??- 怨듦컻 由ъ냼??
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 洹???紐⑤뱺 ?붿껌? ?몄쬆 ?꾩슂
                .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            // ?몄쬆 ?ㅽ뙣 ??401 Unauthorized 諛섑솚 (湲곕낯 403 ???
            .authenticationEntryPoint((request, response, authException) -> {
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              response.setContentType("application/json;charset=UTF-8");
              response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"?몄쬆???꾩슂?⑸땲??\"}");
            })
            // 沅뚰븳 遺議깆떆 403 Forbidden
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              response.setContentType("application/json;charset=UTF-8");
              response.getWriter().write("{\"error\":\"Access Denied\", \"message\":\"沅뚰븳???놁뒿?덈떎.\"}");
            })
        )

        // JWT ?몄쬆 ?꾪꽣 異붽? (UsernamePasswordAuthenticationFilter ?댁쟾???ㅽ뻾)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}


