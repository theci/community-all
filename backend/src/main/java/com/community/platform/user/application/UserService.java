package com.community.platform.user.application;

import com.community.platform.user.domain.*;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.exception.DuplicateUserException;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import com.community.platform.user.infrastructure.persistence.UserProfileRepository;
import com.community.platform.shared.application.DomainEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 관리 애플리케이션 서비스
 * 사용자 생명주기와 관련된 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventService domainEventService;

    /**
     * 새 사용자 회원가입 처리
     */
    @Transactional
    public User registerUser(String email, String password, String nickname) {
        log.info("사용자 회원가입 처리 시작. email: {}, nickname: {}", email, nickname);
        
        // 이메일 중복 체크
        validateEmailNotDuplicated(email);
        
        // 닉네임 중복 체크
        validateNicknameNotDuplicated(nickname);
        
        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(password);
        
        // 사용자 생성 및 저장
        User user = User.register(email, encryptedPassword, nickname);
        User savedUser = userRepository.save(user);

        // 기본 프로필 생성
        UserProfile profile = UserProfile.createFor(savedUser);
        userProfileRepository.save(profile);

        // 회원가입 이벤트 추가 (ID가 생성된 후)
        savedUser.publishRegisteredEvent();

        // 도메인 이벤트 발행 (회원가입 완료)
        domainEventService.publishEvents(savedUser);
        
        log.info("사용자 회원가입 완료. userId: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * 사용자 로그인 처리
     */
    @Transactional
    public User login(String email, String password) {
        log.info("사용자 로그인 시도. email: {}", email);
        
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // 사용자 상태 확인
        if (user.isBlocked()) {
            throw new IllegalStateException("차단된 사용자입니다.");
        }
        
        if (!user.isActive()) {
            throw new IllegalStateException("비활성 상태의 사용자입니다.");
        }
        
        // 마지막 로그인 시간 업데이트
        user.updateLastLoginAt();
        
        log.info("사용자 로그인 성공. userId: {}", user.getId());
        return user;
    }

    /**
     * 사용자 정보 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * 이메일로 사용자 조회
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    /**
     * 사용자 프로필과 함께 조회
     */
    public UserWithProfile getUserWithProfile(Long userId) {
        User user = getUserById(userId);
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(null);
        
        return new UserWithProfile(user, profile);
    }

    /**
     * 사용자 프로필 업데이트
     */
    @Transactional
    public void updateUserProfile(Long userId, String profileImageUrl, String bio, 
                                 LocalDate birthDate, UserProfile.Gender gender) {
        log.info("사용자 프로필 업데이트. userId: {}", userId);
        
        // 사용자 존재 확인
        getUserById(userId);
        
        // 프로필 조회 또는 생성
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = getUserById(userId);
                    return UserProfile.createFor(user);
                });
        
        // 프로필 정보 업데이트
        profile.updateProfile(profileImageUrl, bio, birthDate, gender);
        userProfileRepository.save(profile);
        
        log.info("사용자 프로필 업데이트 완료. userId: {}", userId);
    }

    /**
     * 사용자 상태 변경 - 차단
     */
    @Transactional
    public void blockUser(Long userId, String reason) {
        log.info("사용자 차단 처리. userId: {}, reason: {}", userId, reason);
        
        User user = getUserById(userId);
        user.block(reason);
        
        // 도메인 이벤트 발행 (사용자 차단)
        domainEventService.publishEvents(user);
        
        log.info("사용자 차단 완료. userId: {}", userId);
    }

    /**
     * 사용자 상태 변경 - 활성화
     */
    @Transactional
    public void activateUser(Long userId) {
        log.info("사용자 활성화 처리. userId: {}", userId);
        
        User user = getUserById(userId);
        user.activate();
        
        log.info("사용자 활성화 완료. userId: {}", userId);
    }

    /**
     * 사용자 상태 변경 - 비활성화
     */
    @Transactional
    public void deactivateUser(Long userId) {
        log.info("사용자 비활성화 처리. userId: {}", userId);
        
        User user = getUserById(userId);
        user.deactivate();
        
        log.info("사용자 비활성화 완료. userId: {}", userId);
    }

    /**
     * 활성 사용자 목록 조회 (페이징)
     */
    public Page<User> getActiveUsers(Pageable pageable) {
        return userRepository.findByStatusOrderByCreatedAtDesc(UserStatus.ACTIVE, pageable);
    }

    /**
     * 사용자 검색 (닉네임 기반)
     */
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByNicknameContainingAndStatus(
                keyword, UserStatus.ACTIVE, pageable);
    }

    /**
     * 회원가입 통계 조회 (기간별)
     */
    public Long getRegistrationCount(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.countByCreatedAtBetween(startDate, endDate);
    }

    /**
     * 휴면 계정 조회 (N일 이상 미접속)
     */
    public List<User> getInactiveUsers(int daysAgo) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysAgo);
        return userRepository.findInactiveUsers(threshold);
    }

    /**
     * 이메일 중복 체크
     */
    private void validateEmailNotDuplicated(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("이메일", email);
        }
    }

    /**
     * 닉네임 중복 체크
     */
    private void validateNicknameNotDuplicated(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new DuplicateUserException("닉네임", nickname);
        }
    }

    /**
     * 사용자와 프로필 정보를 함께 담는 DTO 클래스
     */
    public static class UserWithProfile {
        private final User user;
        private final UserProfile profile;

        public UserWithProfile(User user, UserProfile profile) {
            this.user = user;
            this.profile = profile;
        }

        public User getUser() { return user; }
        public UserProfile getProfile() { return profile; }
        public boolean hasProfile() { return profile != null; }
    }
}