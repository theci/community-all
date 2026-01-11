package com.community.platform.shared.security.filter;

import com.community.platform.shared.security.JwtException;
import com.community.platform.shared.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 기반 인증 필터
 * - HTTP 요청에서 JWT 토큰을 추출
 * - 토큰 유효성 검증 후 SecurityContext에 인증 정보 설정
 * - 인증 실패 시 적절한 에러 응답 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // JWT 토큰 추출
            String token = resolveToken(request);
            
            if (StringUtils.hasText(token)) {
                // 토큰 유효성 검증
                if (jwtTokenProvider.validateToken(token)) {
                    // 액세스 토큰인지 확인
                    if (jwtTokenProvider.isAccessToken(token)) {
                        // Authentication 객체 생성 및 SecurityContext에 설정
                        Authentication authentication = jwtTokenProvider.getAuthentication(token);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("JWT 인증 성공: userId={}", 
                            jwtTokenProvider.getUserId(token));
                    } else {
                        log.warn("리프레시 토큰을 액세스 토큰으로 사용 시도");
                        handleJwtException(response, "액세스 토큰이 필요합니다", "INVALID_TOKEN_TYPE");
                        return;
                    }
                }
            }
            
            filterChain.doFilter(request, response);
            
        } catch (JwtException e) {
            log.warn("JWT 인증 실패: {}", e.getMessage());
            handleJwtException(response, e.getMessage(), e.getErrorCode());
        } catch (Exception e) {
            log.error("인증 필터에서 예상치 못한 오류 발생", e);
            handleJwtException(response, "인증 처리 중 오류가 발생했습니다", "AUTHENTICATION_ERROR");
        }
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     * 우선순위: Cookie > Authorization Header
     */
    private String resolveToken(HttpServletRequest request) {
        // 1. 쿠키에서 토큰 추출 시도
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (StringUtils.hasText(token)) {
                        log.debug("쿠키에서 액세스 토큰 추출 성공");
                        return token;
                    }
                }
            }
        }

        // 2. Authorization 헤더에서 토큰 추출 (fallback)
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            log.debug("Authorization 헤더에서 액세스 토큰 추출 성공");
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * JWT 예외 발생 시 에러 응답 생성
     */
    private void handleJwtException(HttpServletResponse response, String message, String errorCode) 
            throws IOException {
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
        
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 인증이 필요하지 않은 경로는 필터를 적용하지 않음
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 인증이 필요한 게시글 관련 경로들
        if (path.startsWith("/api/v1/posts/following") ||
            path.startsWith("/api/v1/posts/likes") ||
            path.startsWith("/api/v1/posts/scraps")) {
            return false; // 필터 적용 (인증 필요)
        }

        // 공개 API 경로
        return path.startsWith("/api/v1/users/register") ||
               path.startsWith("/api/v1/users/login") ||
               path.startsWith("/api/v1/users/check-email") ||
               path.startsWith("/api/v1/users/check-nickname") ||
               path.startsWith("/api/v1/categories") ||
               (path.startsWith("/api/v1/posts") && method.equals("GET")) ||
               path.startsWith("/api/v1/scrap-folders/public") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator");
    }
}