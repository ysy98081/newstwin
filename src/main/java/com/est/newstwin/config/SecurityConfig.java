package com.est.newstwin.config;

import com.est.newstwin.config.jwt.JwtAuthenticationFilter;
import com.est.newstwin.config.oauth2.CustomOAuth2UserService;
import com.est.newstwin.config.oauth2.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT + API 사용
                // - 뷰(폼 전송)에는 CSRF 토큰 사용
                // - /api/**, H2 콘솔 등은 CSRF 검사에서 제외
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/api/**",         // REST API는 JWT로만 보호 (CSRF 검사 제외)
                                "/h2-console/**",   // H2 콘솔
                                "/board/write",
                                "/board/edit/**",
                                "/board/delete/**"
                        )
                )

                // H2 콘솔 사용을 위해 frameOptions 허용
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // 세션을 전혀 사용하지 않음 (완전한 stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // 정적 리소스 허용 (js, css, images 등)
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/favicon.ico",
                                "/webjars/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/unsubscribe"
                        ).permitAll()

                        // 로그인 / 회원가입 페이지는 누구나 접근 가능
                        .requestMatchers("/login", "/signup").permitAll()

                        // 관리자 로그인 페이지는 누구나 접근 가능
                        .requestMatchers("/admin/login").permitAll()

                        // 인증 없이 접근 가능한 페이지
                        .requestMatchers(
                                "/",                 // 홈
                                "/verify",           // 이메일 인증 결과 페이지
                                "/verify-info",      // 이메일 인증 안내 페이지
                                "/news/**",          // 뉴스 관련 페이지
                                "/feed",             // 뉴스 카테고리
                                "/h2-console/**",    // H2 콘솔
                                "/post/**"           // 뉴스 상세
                        ).permitAll()

                        // 인증 없이 접근 가능한 API
                        .requestMatchers(
                                "/api/auth/**",                // 로그인/로그아웃 API
                                "/api/members/signup",         // 회원가입 API
                                "/api/members/verify",         // API 방식 인증 (모바일/테스트용)
                                "/api/members/resend-verification",
                                "/api/members/me",             // 로그인 확인 API
                                "/api/members/check-email",    // 이메일 중복 확인 API
                                "/api/members/exists",         // 이메일 존재 여부 확인
                                "/api/chatgpt/**",
                                "/api/alan/**",
                                "/api/pipeline/**",
                                "/api/home/**"
                        ).permitAll()

                        // 게시판 접근 정책
                        .requestMatchers(
                                "/board",
                                "/board/",
                                "/board/{id:[0-9]+}"
                        ).permitAll()

                        .requestMatchers(
                                "/board/write",
                                "/board/edit/**",
                                "/board/delete/**"
                        ).authenticated()

                        // 좋아요/북마크 조회는 누구나 허용
                        .requestMatchers(HttpMethod.GET,
                                "/api/posts/*/like",
                                "/api/posts/*/like/count",
                                "/api/posts/*/bookmark",
                                "/api/posts/*/comments"
                        ).permitAll()

                        // 토글(변경)은 로그인 필요
                        .requestMatchers(HttpMethod.POST,
                                "/api/posts/*/like",
                                "/api/posts/*/bookmark",
                                "/api/posts/*/comments"
                        ).authenticated()

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/posts/comments/*"
                        ).authenticated()

                        // 마이페이지는 로그인 필요
                        .requestMatchers("/mypage/**").authenticated()

                        // 관리자 수동 실행용, 관리자 전용
                        .requestMatchers("/api/scheduler/**").hasAuthority("ROLE_ADMIN")

                        // 그 외 /admin/** 는 ROLE_ADMIN만 접근
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        // 나머지 요청은 기본적으로 인증 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // 커스텀 로그인 페이지
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )

                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 접근 거부/미인증 처리
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String uri = request.getRequestURI();

                            // 댓글 / 좋아요 / 북마크 API 전용 401 처리
                            if (uri.startsWith("/api/posts") &&
                                    (uri.contains("/like") || uri.contains("/bookmark") || uri.contains("/comments"))) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                return;
                            }

                            // 관리자 영역
                            if (uri.startsWith("/admin")) {
                                response.sendRedirect("/admin/login");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // CSRF 등 권한 부족 시
                            response.sendRedirect("/");
                        })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
