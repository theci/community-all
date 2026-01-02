package com.community.platform.user.application;

import com.community.platform.shared.security.SecurityUtils;
import com.community.platform.shared.security.jwt.JwtTokenProvider;
import com.community.platform.user.domain.User;
import com.community.platform.user.exception.InvalidCredentialsException;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 인증 관련 비즈니스 로직을 담당하는 서비스
 * - JWT 기반 로그인/로그아웃
 * - 토큰 갱신 (Refresh Token)
 * - 사용자 인증 정보 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 이메일/패스워드 기반 로그인
     * @param email 사용자 이메일
     * @param password 사용자 패스워드
     * @return JWT 토큰 정보
     */
    @Transactional(readOnly = true)
    public TokenInfo login(String email, String password) {
        log.info("사용자 로그인 시도: email={}", email);
        
        try {
            // Spring Security를 통한 인증
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
            
            // 인증 성공 시 사용자 정보 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(email));
            
            // JWT 토큰 생성
            List<String> authorities = Arrays.asList(user.getRole().getAuthority());
            String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getEmail(), authorities);
            String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getId(), user.getEmail());
            
            log.info("로그인 성공: userId={}, email={}", user.getId(), email);
            
            return TokenInfo.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1시간 (설정값과 동기화 필요)
                    .build();
            
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 인증 정보: email={}", email);
            throw new InvalidCredentialsException();
        }
    }

    /**
     * 리프레시 토큰을 사용한 액세스 토큰 갱신
     * @param refreshToken 리프레시 토큰
     * @return 새로운 JWT 토큰 정보
     */
    @Transactional(readOnly = true)
    public TokenInfo refresh(String refreshToken) {
        log.debug("토큰 갱신 요청");
        
        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidCredentialsException();
        }
        
        // 리프레시 토큰인지 확인
        if (jwtTokenProvider.isAccessToken(refreshToken)) {
            throw new InvalidCredentialsException();
        }
        
        // 토큰에서 사용자 정보 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String email = jwtTokenProvider.getEmail(refreshToken);
        
        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 새로운 토큰 생성
        List<String> authorities = Arrays.asList(user.getRole().getAuthority());
        String newAccessToken = jwtTokenProvider.createAccessToken(
            user.getId(), user.getEmail(), authorities);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(
            user.getId(), user.getEmail());
        
        log.debug("토큰 갱신 완료: userId={}", userId);
        
        return TokenInfo.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();
    }

    /**
     * 현재 사용자 로그아웃
     * 실제로는 클라이언트에서 토큰을 삭제하는 것으로 처리
     * 서버사이드에서는 토큰 블랙리스트 관리 등을 추가할 수 있음
     */
    public void logout() {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new InvalidCredentialsException());
        
        log.info("사용자 로그아웃: userId={}", currentUserId);
        
        // TODO: 토큰 블랙리스트 처리 (Redis 등 사용)
        // TODO: 리프레시 토큰 무효화 처리
    }

    /**
     * 현재 토큰의 유효성 검증
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token) && 
                   jwtTokenProvider.isAccessToken(token);
        } catch (Exception e) {
            log.debug("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 사용자 정보 추출
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        if (!validateToken(token)) {
            throw new InvalidCredentialsException();
        }
        
        return jwtTokenProvider.getUserId(token);
    }

    /**
     * JWT 토큰 정보를 담는 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class TokenInfo {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
    }
}