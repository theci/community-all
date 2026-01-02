package com.community.platform.user.infrastructure.persistence;

import com.community.platform.authorization.domain.UserRole;
import com.community.platform.user.domain.User;
import com.community.platform.user.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회 (로그인 시 사용)
    Optional<User> findByEmail(String email);
    
    // 닉네임으로 사용자 조회 (닉네임 중복 체크)
    Optional<User> findByNickname(String nickname);
    
    // 이메일 존재 여부 확인 (회원가입 시 중복 체크)
    boolean existsByEmail(String email);
    
    // 닉네임 존재 여부 확인 (회원가입 시 중복 체크)
    boolean existsByNickname(String nickname);
    
    // 상태별 사용자 조회 (관리자가 사용자 관리할 때 사용)
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    
    // 활성 사용자 목록 조회 (일반적인 사용자 목록)
    Page<User> findByStatusOrderByCreatedAtDesc(UserStatus status, Pageable pageable);
    
    // 특정 기간 동안 가입한 사용자 수 조회 (통계용)
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    // 특정 기간 동안 로그인한 사용자 조회 (활동성 분석)
    @Query("SELECT u FROM User u WHERE u.lastLoginAt BETWEEN :startDate AND :endDate")
    List<User> findByLastLoginAtBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    // 닉네임에 특정 키워드가 포함된 사용자 검색 (사용자 검색 기능)
    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:keyword% AND u.status = :status")
    Page<User> findByNicknameContainingAndStatus(@Param("keyword") String keyword, 
                                                 @Param("status") UserStatus status, 
                                                 Pageable pageable);
    
    // 오랫동안 로그인하지 않은 사용자 조회 (휴면 계정 관리)
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :beforeDate OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("beforeDate") LocalDateTime beforeDate);
    
    // 특정 사용자들의 ID 목록으로 조회 (배치 처리용)
    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    List<User> findByIdIn(@Param("userIds") List<Long> userIds);

    // ========== 역할(Role) 관련 메서드 (Phase 6-2) ==========

    // 역할별 사용자 수 조회
    long countByRole(UserRole role);

    // 역할별 사용자 목록 조회
    List<User> findByRole(UserRole role);

    // 여러 역할의 사용자 목록 조회 (관리자 목록 등)
    List<User> findByRoleIn(List<UserRole> roles);

    // 역할별 사용자 목록 조회 (페이징)
    Page<User> findByRole(UserRole role, Pageable pageable);

    // ========== 통계 관련 메서드 (Phase 9) ==========

    // 상태별 사용자 수 조회
    long countByStatus(UserStatus status);
}