package com.community.platform.shared.security.jwt;

import com.community.platform.shared.security.JwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성, 검증, 파싱을 담당하는 유틸리티 클래스
 * - 액세스 토큰 및 리프레시 토큰 생성
 * - 토큰 유효성 검증
 * - 토큰에서 사용자 정보 추출
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String USER_ID_KEY = "userId";
    private static final String TOKEN_TYPE_KEY = "tokenType";
    
    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds:3600}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds:1209600}") long refreshTokenValidityInSeconds) {
        
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }

    /**
     * 액세스 토큰 생성
     */
    public String createAccessToken(Long userId, String email, Collection<String> authorities) {
        log.debug("액세스 토큰 생성: userId={}, email={}", userId, email);
        
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + accessTokenValidityInMilliseconds);
        
        return Jwts.builder()
                .setSubject(email)
                .claim(USER_ID_KEY, userId)
                .claim(TOKEN_TYPE_KEY, "ACCESS")
                .claim(AUTHORITIES_KEY, String.join(",", authorities))
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 리프레시 토큰 생성
     */
    public String createRefreshToken(Long userId, String email) {
        log.debug("리프레시 토큰 생성: userId={}, email={}", userId, email);
        
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + refreshTokenValidityInMilliseconds);
        
        return Jwts.builder()
                .setSubject(email)
                .claim(USER_ID_KEY, userId)
                .claim(TOKEN_TYPE_KEY, "REFRESH")
                .setIssuedAt(new Date(now))
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 토큰에서 Authentication 객체 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        
        String email = claims.getSubject();
        Long userId = claims.get(USER_ID_KEY, Long.class);
        String authoritiesStr = claims.get(AUTHORITIES_KEY, String.class);
        
        Collection<? extends GrantedAuthority> authorities = 
            authoritiesStr != null ? 
                Arrays.stream(authoritiesStr.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()) :
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        
        // 사용자 정보를 담은 UserDetails 생성 (userId를 username으로 사용)
        UserDetails principal = User.builder()
                .username(userId.toString())
                .password("")  // 빈 패스워드 (JWT 기반이므로 불필요)
                .authorities(authorities)
                .build();
        
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return claims.get(USER_ID_KEY, Long.class);
    }

    /**
     * 토큰에서 이메일(Subject) 추출
     */
    public String getEmail(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명: {}", e.getMessage());
            throw JwtException.malformed();
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            throw JwtException.expired();
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰: {}", e.getMessage());
            throw JwtException.invalid();
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
            throw JwtException.invalid();
        }
    }

    /**
     * 토큰 타입 확인 (ACCESS/REFRESH)
     */
    public boolean isAccessToken(String token) {
        Claims claims = parseClaims(token);
        return "ACCESS".equals(claims.get(TOKEN_TYPE_KEY, String.class));
    }

    /**
     * 토큰 만료 시간 조회
     */
    public Date getExpirationDate(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration();
    }

    /**
     * 토큰 남은 유효 시간 (밀리초)
     */
    public long getTimeToExpiry(String token) {
        Date expiration = getExpirationDate(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * 토큰이 곧 만료되는지 확인 (30분 이내)
     */
    public boolean isTokenExpiringSoon(String token) {
        return getTimeToExpiry(token) < 30 * 60 * 1000; // 30분
    }

    /**
     * JWT Claims 파싱 (private 메서드)
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰도 Claims는 읽을 수 있음 (리프레시 토큰 검증 등에 필요)
            return e.getClaims();
        }
    }
}