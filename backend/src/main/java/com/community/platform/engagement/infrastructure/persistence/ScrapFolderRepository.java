package com.community.platform.engagement.infrastructure.persistence;

import com.community.platform.engagement.domain.ScrapFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapFolderRepository extends JpaRepository<ScrapFolder, Long> {

    // 특정 사용자의 모든 스크랩 폴더 조회 (마이페이지 폴더 목록용)
    List<ScrapFolder> findByUserIdOrderByCreatedAtAsc(Long userId);
    
    // 특정 사용자의 기본 스크랩 폴더 조회 (기본 폴더 찾기용)
    Optional<ScrapFolder> findByUserIdAndIsDefaultTrue(Long userId);
    
    // 특정 사용자의 폴더명으로 조회 (폴더명 중복 체크용)
    Optional<ScrapFolder> findByUserIdAndName(Long userId, String name);
    
    // 특정 사용자의 폴더명 존재 여부 확인 (폴더명 중복 체크용)
    boolean existsByUserIdAndName(Long userId, String name);
    
    // 특정 사용자의 폴더 개수 조회 (폴더 생성 제한용)
    @Query("SELECT COUNT(sf) FROM ScrapFolder sf WHERE sf.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    // 스크랩 개수와 함께 폴더 조회 (폴더별 스크랩 통계용)
    @Query("SELECT sf, COUNT(ps) as scrapCount FROM ScrapFolder sf " +
           "LEFT JOIN PostScrap ps ON ps.scrapFolder.id = sf.id " +
           "WHERE sf.userId = :userId " +
           "GROUP BY sf " +
           "ORDER BY sf.createdAt ASC")
    List<Object[]> findFoldersWithScrapCountByUserId(@Param("userId") Long userId);
    
    // 빈 스크랩 폴더 조회 (사용하지 않는 폴더 정리용)
    @Query("SELECT sf FROM ScrapFolder sf WHERE sf.userId = :userId AND " +
           "NOT EXISTS (SELECT 1 FROM PostScrap ps WHERE ps.scrapFolder.id = sf.id)")
    List<ScrapFolder> findEmptyFoldersByUserId(@Param("userId") Long userId);
    
    // 특정 사용자의 모든 폴더 삭제 (회원 탈퇴 시 사용)
    void deleteByUserId(Long userId);
    
    // 폴더명으로 검색 (폴더 검색 기능용)
    @Query("SELECT sf FROM ScrapFolder sf WHERE sf.userId = :userId AND sf.name LIKE %:keyword%")
    List<ScrapFolder> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    // 최근 생성된 폴더 조회 (최근 활동 추적용)
    @Query("SELECT sf FROM ScrapFolder sf WHERE sf.userId = :userId ORDER BY sf.createdAt DESC")
    List<ScrapFolder> findRecentFoldersByUserId(@Param("userId") Long userId);
}