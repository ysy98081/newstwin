package com.est.newstwin.config;

import com.est.newstwin.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT 사용 시 CSRF 비활성화
                .csrf(csrf -> csrf.disable())

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
                                "/favicon.ico",
                                "/webjars/**"
                        ).permitAll()

                        // 인증 없이 접근 가능한 페이지
                        .requestMatchers(
                                "/",                    // 홈
                                "/login",               // 로그인 페이지
                                "/signup",              // 회원가입 페이지
                                "/news/**",             // 뉴스 관련 페이지
                                "/feed",                // 뉴스 카테고리
                                "/h2-console/**"        // H2 콘솔
                        ).permitAll()

                        // 인증 없이 접근 가능한 API
                        .requestMatchers(
                                "/api/auth/**",             // 로그인/로그아웃 API
                                "/api/members/signup",      // 회원가입 API
                                "/api/members/me",          // (개발용) 로그인 확인 API
                                "/api/members/check-email"  // 이메일 중복 확인 API
                        ).permitAll()

                        // 그 외 페이지 중 로그인 필요한 부분
                        .requestMatchers(
                                "/mypage/**"
                        ).authenticated()

                        // 관리자 전용 페이지 (ADMIN 권한 필요)
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        // 나머지 요청은 기본적으로 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
