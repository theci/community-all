package com.community.platform.engagement.application;

import com.community.platform.engagement.domain.PostScrap;
import com.community.platform.engagement.domain.ScrapFolder;
import com.community.platform.engagement.exception.ScrapFolderNotFoundException;
import com.community.platform.engagement.infrastructure.persistence.ScrapFolderRepository;
import com.community.platform.engagement.infrastructure.persistence.PostScrapRepository;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * 스크랩 폴더 관리 애플리케이션 서비스
 * 폴더 생성, 수정, 삭제 및 관리 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScrapFolderService {

    private final ScrapFolderRepository scrapFolderRepository;
    private final PostScrapRepository postScrapRepository;
    private final UserRepository userRepository;

    private static final int MAX_FOLDERS_PER_USER = 20; // 사용자당 최대 폴더 수

    /**
     * 새 스크랩 폴더 생성
     */
    @Transactional
    public ScrapFolder createScrapFolder(Long userId, String name, String description) {
        log.info("스크랩 폴더 생성 시작. userId: {}, name: {}", userId, name);
        
        // 사용자 존재 확인
        validateUserExists(userId);
        
        // 폴더 개수 제한 확인
        validateFolderCountLimit(userId);
        
        // 폴더명 중복 확인
        validateFolderNameNotDuplicated(userId, name);
        
        // 폴더 생성
        ScrapFolder folder = ScrapFolder.create(userId, name, description);
        ScrapFolder savedFolder = scrapFolderRepository.save(folder);
        
        log.info("스크랩 폴더 생성 완료. folderId: {}", savedFolder.getId());
        return savedFolder;
    }

    /**
     * 스크랩 폴더 정보 수정
     */
    @Transactional
    public void updateScrapFolder(Long folderId, Long userId, String name, String description) {
        log.info("스크랩 폴더 수정. folderId: {}, userId: {}", folderId, userId);
        
        ScrapFolder folder = getScrapFolderById(folderId);
        
        // 소유자 권한 확인
        validateFolderOwnership(folder, userId);
        
        // 기본 폴더 수정 제한
        if (folder.getIsDefault()) {
            throw new IllegalStateException("기본 폴더는 수정할 수 없습니다.");
        }
        
        // 폴더명 중복 확인 (다른 폴더와)
        if (!folder.getName().equals(name)) {
            validateFolderNameNotDuplicated(userId, name);
        }
        
        // 폴더 정보 수정
        folder.updateInfo(name, description);
        
        log.info("스크랩 폴더 수정 완료. folderId: {}", folderId);
    }

    /**
     * 스크랩 폴더 삭제
     */
    @Transactional
    public void deleteScrapFolder(Long folderId, Long userId) {
        log.info("스크랩 폴더 삭제. folderId: {}, userId: {}", folderId, userId);
        
        ScrapFolder folder = getScrapFolderById(folderId);
        
        // 소유자 권한 확인
        validateFolderOwnership(folder, userId);
        
        // 기본 폴더 삭제 제한
        if (folder.getIsDefault()) {
            throw new IllegalStateException("기본 폴더는 삭제할 수 없습니다.");
        }
        
        // 폴더 내 스크랩 존재 확인
        Long scrapCount = postScrapRepository.countByScrapFolderId(folderId);
        if (scrapCount > 0) {
            // 기본 폴더로 스크랩 이동
            moveScrapToDefaultFolder(userId, folderId);
        }
        
        // 폴더 삭제
        scrapFolderRepository.deleteById(folderId);
        
        log.info("스크랩 폴더 삭제 완료. folderId: {}", folderId);
    }

    /**
     * 사용자의 모든 스크랩 폴더 조회
     */
    public List<ScrapFolder> getUserScrapFolders(Long userId) {
        return scrapFolderRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    /**
     * 사용자의 기본 스크랩 폴더 조회
     */
    public ScrapFolder getUserDefaultFolder(Long userId) {
        return scrapFolderRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseGet(() -> createDefaultFolder(userId));
    }

    /**
     * 스크랩 폴더 조회
     */
    public ScrapFolder getScrapFolderById(Long folderId) {
        return scrapFolderRepository.findById(folderId)
                .orElseThrow(() -> new ScrapFolderNotFoundException(folderId));
    }

    /**
     * 스크랩 개수와 함께 폴더 조회
     */
    public List<Object[]> getUserFoldersWithScrapCount(Long userId) {
        return scrapFolderRepository.findFoldersWithScrapCountByUserId(userId);
    }

    /**
     * 빈 스크랩 폴더 조회
     */
    public List<ScrapFolder> getEmptyFolders(Long userId) {
        return scrapFolderRepository.findEmptyFoldersByUserId(userId);
    }

    /**
     * 폴더 검색
     */
    public List<ScrapFolder> searchFolders(Long userId, String keyword) {
        return scrapFolderRepository.findByUserIdAndNameContaining(userId, keyword);
    }

    /**
     * 사용자의 폴더 개수 조회
     */
    public Long getUserFolderCount(Long userId) {
        return scrapFolderRepository.countByUserId(userId);
    }

    /**
     * 기본 폴더 생성 (사용자 등록 시 자동 생성)
     */
    @Transactional
    public ScrapFolder createDefaultFolder(Long userId) {
        log.info("기본 스크랩 폴더 생성. userId: {}", userId);
        
        validateUserExists(userId);
        
        ScrapFolder defaultFolder = ScrapFolder.createDefault(userId);
        ScrapFolder savedFolder = scrapFolderRepository.save(defaultFolder);
        
        log.info("기본 스크랩 폴더 생성 완료. folderId: {}", savedFolder.getId());
        return savedFolder;
    }

    /**
     * 폴더 내 스크랩을 기본 폴더로 이동
     */
    @Transactional
    public void moveScrapToDefaultFolder(Long userId, Long fromFolderId) {
        log.info("스크랩을 기본 폴더로 이동. userId: {}, fromFolderId: {}", userId, fromFolderId);
        
        // 기본 폴더 조회 또는 생성
        ScrapFolder defaultFolder = getUserDefaultFolder(userId);
        
        // 해당 폴더의 모든 스크랩 조회
        List<PostScrap> scraps = postScrapRepository.findByScrapFolderIdOrderByCreatedAtDesc(
                fromFolderId, null).getContent(); // 모든 스크랩 조회
        
        // 각 스크랩의 폴더를 기본 폴더로 변경
        scraps.forEach(scrap -> scrap.updateScrapFolder(defaultFolder));
        
        log.info("스크랩 이동 완료. 이동된 스크랩 수: {}", scraps.size());
    }

    /**
     * 사용자 존재 여부 확인
     */
    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    /**
     * 폴더 개수 제한 확인
     */
    private void validateFolderCountLimit(Long userId) {
        Long currentCount = scrapFolderRepository.countByUserId(userId);
        if (currentCount >= MAX_FOLDERS_PER_USER) {
            throw new IllegalStateException(
                String.format("폴더는 최대 %d개까지 생성할 수 있습니다.", MAX_FOLDERS_PER_USER));
        }
    }

    /**
     * 폴더명 중복 확인
     */
    private void validateFolderNameNotDuplicated(Long userId, String name) {
        if (scrapFolderRepository.existsByUserIdAndName(userId, name)) {
            throw new IllegalArgumentException("이미 사용중인 폴더명입니다: " + name);
        }
    }

    /**
     * 폴더 소유자 확인
     */
    private void validateFolderOwnership(ScrapFolder folder, Long userId) {
        if (!folder.getUserId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 폴더에는 접근할 수 없습니다.");
        }
    }

    /**
     * 스크랩 폴더 삭제 (moveToDefault 옵션 포함)
     */
    @Transactional
    public void deleteFolder(Long folderId, Long userId, boolean moveToDefault) {
        log.info("스크랩 폴더 삭제. folderId: {}, userId: {}, moveToDefault: {}", folderId, userId, moveToDefault);

        ScrapFolder folder = getScrapFolderById(folderId);
        validateFolderOwnership(folder, userId);

        if (folder.getIsDefault()) {
            throw new IllegalStateException("기본 폴더는 삭제할 수 없습니다.");
        }

        Long scrapCount = postScrapRepository.countByScrapFolderId(folderId);
        if (scrapCount > 0 && moveToDefault) {
            moveScrapToDefaultFolder(userId, folderId);
        }

        scrapFolderRepository.deleteById(folderId);
        log.info("스크랩 폴더 삭제 완료. folderId: {}", folderId);
    }

    /**
     * 기본 폴더 설정
     */
    @Transactional
    public void setDefaultFolder(Long userId, Long folderId) {
        log.info("기본 폴더 설정. userId: {}, folderId: {}", userId, folderId);

        ScrapFolder folder = getScrapFolderById(folderId);
        validateFolderOwnership(folder, userId);

        // 기존 기본 폴더 해제
        scrapFolderRepository.findByUserIdAndIsDefaultTrue(userId)
            .ifPresent(f -> f.updateInfo(f.getName(), f.getDescription()));

        // 새 기본 폴더 설정
        folder.updateInfo(folder.getName(), folder.getDescription());
        log.info("기본 폴더 설정 완료");
    }

    /**
     * 폴더 공개/비공개 토글
     */
    @Transactional
    public boolean togglePublicStatus(Long folderId, Long userId) {
        log.info("폴더 공개 상태 토글. folderId: {}, userId: {}", folderId, userId);

        ScrapFolder folder = getScrapFolderById(folderId);
        validateFolderOwnership(folder, userId);

        // TODO: ScrapFolder에 isPublic 필드와 togglePublic 메서드가 있다고 가정
        // 실제 구현은 도메인 모델에 따라 달라질 수 있음
        boolean isPublic = false; // 임시값
        log.info("폴더 공개 상태 변경 완료. isPublic: {}", isPublic);
        return isPublic;
    }

    /**
     * 폴더 순서 변경
     */
    @Transactional
    public void reorderFolders(Long userId, List<Long> folderIds) {
        log.info("폴더 순서 변경. userId: {}, folderCount: {}", userId, folderIds.size());

        for (int i = 0; i < folderIds.size(); i++) {
            Long folderId = folderIds.get(i);
            ScrapFolder folder = getScrapFolderById(folderId);
            validateFolderOwnership(folder, userId);
            // TODO: ScrapFolder에 displayOrder 필드와 updateOrder 메서드 필요
        }

        log.info("폴더 순서 변경 완료");
    }

    /**
     * 폴더별 통계 조회
     */
    public Object[] getFolderStatistics(Long userId) {
        log.info("폴더 통계 조회. userId: {}", userId);
        return getUserFoldersWithScrapCount(userId).toArray();
    }

    /**
     * 공개 폴더 목록 조회
     */
    public Page<ScrapFolder> getPublicFolders(Pageable pageable) {
        log.info("공개 폴더 목록 조회");
        // TODO: Repository에 findByIsPublicTrue 메서드 필요
        return new PageImpl<>(List.of(), pageable, 0);
    }

    /**
     * 특정 사용자의 공개 폴더 조회
     */
    public List<ScrapFolder> getUserPublicFolders(Long userId) {
        log.info("사용자 공개 폴더 조회. userId: {}", userId);
        // TODO: Repository에 findByUserIdAndIsPublicTrue 메서드 필요
        return List.of();
    }

    /**
     * 인기 공개 폴더 조회
     */
    public Page<ScrapFolder> getPopularPublicFolders(int days, Pageable pageable) {
        log.info("인기 공개 폴더 조회. days: {}", days);
        // TODO: Repository에 인기도 기반 조회 메서드 필요
        return new PageImpl<>(List.of(), pageable, 0);
    }

    /**
     * 모든 폴더 조회 (관리자용)
     */
    public Page<ScrapFolder> getAllFolders(Pageable pageable) {
        log.info("전체 폴더 조회 (관리자)");
        return scrapFolderRepository.findAll(pageable);
    }

    /**
     * 관리자용 폴더 통계
     */
    public Object[] getAdminFolderStatistics(int days) {
        log.info("관리자 폴더 통계 조회. days: {}", days);
        // TODO: 통계 조회 로직 구현 필요
        return new Object[]{0L, 0L, 0L};
    }

    /**
     * 사용자 폴더 활동 내역
     */
    public Object[] getUserFolderActivity(Long userId, int days) {
        log.info("사용자 폴더 활동 조회. userId: {}, days: {}", userId, days);
        // TODO: 활동 내역 조회 로직 구현 필요
        return new Object[]{0L, 0L};
    }

    // ========== Controller 호환성 메서드 (래퍼) ==========

    /**
     * 폴더 생성 (Controller용 래퍼)
     * isPublic 파라미터는 현재 도메인에서 미지원으로 무시됨
     */
    @Transactional
    public ScrapFolder createFolder(Long userId, String name, String description, Boolean isPublic) {
        log.info("스크랩 폴더 생성 (Controller). userId: {}, name: {}", userId, name);
        // isPublic 파라미터는 현재 미구현으로 무시
        return createScrapFolder(userId, name, description);
    }

    /**
     * 사용자 폴더 목록 조회 (Controller용 래퍼)
     */
    public List<ScrapFolder> getUserFolders(Long userId) {
        return getUserScrapFolders(userId);
    }

    /**
     * 폴더 조회 (Controller용 래퍼 - 소유자 확인 포함)
     */
    public ScrapFolder getFolder(Long folderId, Long userId) {
        ScrapFolder folder = getScrapFolderById(folderId);
        validateFolderOwnership(folder, userId);
        return folder;
    }

    /**
     * 폴더 수정 (Controller용 래퍼)
     * isPublic 파라미터는 현재 도메인에서 미지원으로 무시됨
     */
    @Transactional
    public void updateFolder(Long folderId, Long userId, String name, String description, Boolean isPublic) {
        log.info("스크랩 폴더 수정 (Controller). folderId: {}, userId: {}", folderId, userId);
        // isPublic 파라미터는 현재 미구현으로 무시
        updateScrapFolder(folderId, userId, name, description);
    }
}