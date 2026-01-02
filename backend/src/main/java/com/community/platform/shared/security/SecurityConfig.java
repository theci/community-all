package com.community.platform.shared.security;

import com.community.platform.shared.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정
 * - JWT 기반 인증 설정
 * - CORS 설정
 * - 보안 필터 체인 구성
 * - 공개 API 경로 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize, @PostAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Security Filter Chain 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF 비활성화 (JWT 사용하므로)
                .csrf(AbstractHttpConfigurer::disable)
                
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // 세션 비활성화 (JWT 사용하므로)
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 요청별 인증 설정
                .authorizeHttpRequests(auth -> auth
                    // 공개 API (인증 불필요)
                    .requestMatchers(
                        "/api/v1/users/register",
                        "/api/v1/users/check-email",
                        "/api/v1/users/check-nickname",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/validate"
                    ).permitAll()
                    
                    // 카테고리 조회 (공개)
                    .requestMatchers(
                        "/api/v1/categories/**"
                    ).permitAll()
                    
                    // 게시글 조회 (공개)
                    .requestMatchers("GET", "/api/v1/posts/**").permitAll()
                    .requestMatchers("GET", "/api/v1/comments/**").permitAll()
                    
                    // 공개 스크랩 폴더 조회
                    .requestMatchers("GET", "/api/v1/scrap-folders/public/**").permitAll()
                    .requestMatchers("GET", "/api/v1/scrap-folders/user/*/public").permitAll()
                    
                    // 개발 도구 (개발 환경에서만)
                    .requestMatchers(
                        "/h2-console/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/**"
                    ).permitAll()
                    
                    // 관리자 API (ADMIN 권한 필요)
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    
                    // 나머지 API (인증 필요)
                    .anyRequest().authenticated()
                )
                
                // JWT 필터 추가 (UsernamePasswordAuthenticationFilter 전에)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                
                // 예외 처리 핸들러 설정
                .exceptionHandling(exceptions -> exceptions
                    // 인증 실패 시
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                            "{\"success\":false,\"message\":\"인증이 필요합니다\",\"errorCode\":\"AUTHENTICATION_REQUIRED\"}"
                        );
                    })
                    // 권한 없음 시
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                            "{\"success\":false,\"message\":\"접근 권한이 없습니다\",\"errorCode\":\"ACCESS_DENIED\"}"
                        );
                    })
                )
                
                // H2 콘솔을 위한 frame options 비활성화 (개발 환경에서만)
                .headers(headers -> headers.frameOptions().sameOrigin())
                
                .build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 Origin 설정
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 개발 환경용, 프로덕션에서는 구체적인 도메인 지정
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 자격 증명 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);
        
        // 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * 패스워드 인코더 설정
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager 설정
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return new ProviderManager(authProvider);
    }
}