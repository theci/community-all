package com.community.platform.shared.security;

/**
 * JWT 관련 예외
 */
public class JwtException extends SecurityException {
    
    public JwtException(String message) {
        super("JWT_ERROR", message);
    }
    
    public JwtException(String message, Throwable cause) {
        super("JWT_ERROR", message, cause);
    }
    
    public static JwtException expired() {
        return new JwtException("JWT 토큰이 만료되었습니다");
    }
    
    public static JwtException invalid() {
        return new JwtException("유효하지 않은 JWT 토큰입니다");
    }
    
    public static JwtException missing() {
        return new JwtException("JWT 토큰이 없습니다");
    }
    
    public static JwtException malformed() {
        return new JwtException("JWT 토큰 형식이 올바르지 않습니다");
    }
}