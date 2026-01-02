package com.community.platform.user.infrastructure.persistence;

import com.community.platform.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // 사용자 ID로 프로필 조회 (프로필 페이지에서 사용)
    Optional<UserProfile> findByUserId(Long userId);
    
    // 사용자 ID로 프로필 존재 여부 확인 (프로필 생성 전 체크)
    boolean existsByUserId(Long userId);
    
    // 전화번호로 프로필 조회 (전화번호 중복 체크)
    @Query("SELECT up FROM UserProfile up WHERE up.phoneNumber = :phoneNumber")
    Optional<UserProfile> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    // 전화번호 존재 여부 확인 (전화번호 중복 체크용)
    @Query("SELECT COUNT(up) > 0 FROM UserProfile up WHERE up.phoneNumber = :phoneNumber")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    // 사용자 ID와 함께 사용자 정보도 가져오기 (한번의 쿼리로 처리)
    @Query("SELECT up FROM UserProfile up JOIN FETCH up.user WHERE up.user.id = :userId")
    Optional<UserProfile> findByUserIdWithUser(@Param("userId") Long userId);
}