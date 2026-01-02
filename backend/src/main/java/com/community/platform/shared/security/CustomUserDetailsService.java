package com.community.platform.shared.security;

import com.community.platform.user.domain.User;
import com.community.platform.user.domain.UserStatus;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.exception.UserStatusException;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetailsService 구현체
 * 사용자 인증 정보를 데이터베이스에서 조회하여 Spring Security UserDetails로 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 이메일로 사용자 정보를 조회하여 UserDetails 반환
     * Spring Security 로그인 처리에 사용됨
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("사용자 인증 정보 조회: email={}", email);
        
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(email));
            
            // 사용자 상태 검증
            validateUserStatus(user);
            
            return new CustomUserDetails(user);
            
        } catch (UserNotFoundException e) {
            log.warn("존재하지 않는 사용자 로그인 시도: email={}", email);
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
        } catch (UserStatusException e) {
            log.warn("비정상 상태 사용자 로그인 시도: email={}, status={}", email, e.getMessage());
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    /**
     * 사용자 ID로 사용자 정보를 조회하여 UserDetails 반환
     * JWT 토큰 검증 시 사용됨
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        log.debug("사용자 인증 정보 조회: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 사용자 상태 검증
        validateUserStatus(user);
        
        return new CustomUserDetails(user);
    }

    /**
     * 사용자 상태 검증
     */
    private void validateUserStatus(User user) {
        switch (user.getStatus()) {
            case BLOCKED:
                throw UserStatusException.blocked();
            case INACTIVE:
                throw UserStatusException.inactive();
            case ACTIVE:
                // 정상 상태
                break;
            default:
                throw new UserStatusException("알 수 없는 사용자 상태입니다");
        }
    }

    /**
     * 커스텀 UserDetails 구현체
     * User 엔티티를 Spring Security UserDetails로 어댑팅
     */
    public static class CustomUserDetails implements UserDetails {
        
        private final User user;
        
        public CustomUserDetails(User user) {
            this.user = user;
        }
        
        // User 엔티티 접근자
        public User getUser() {
            return user;
        }
        
        public Long getUserId() {
            return user.getId();
        }
        
        public String getEmail() {
            return user.getEmail();
        }
        
        public String getNickname() {
            return user.getNickname();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // 기본적으로 ROLE_USER 권한 부여
            // 향후 Role/Permission 테이블 추가 시 확장 가능
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            // JWT에서는 userId를 username으로 사용
            return user.getId().toString();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true; // 계정 만료 기능 미구현
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getStatus() != UserStatus.BLOCKED;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true; // 패스워드 만료 기능 미구현
        }

        @Override
        public boolean isEnabled() {
            return user.getStatus() == UserStatus.ACTIVE;
        }
    }
}