package com.community.platform.admin.application;

import com.community.platform.admin.dto.*;
import com.community.platform.authorization.domain.UserRole;
import com.community.platform.content.domain.PostStatus;
import com.community.platform.content.infrastructure.persistence.CommentRepository;
import com.community.platform.content.infrastructure.persistence.PostRepository;
import com.community.platform.engagement.infrastructure.persistence.PostLikeRepository;
import com.community.platform.engagement.infrastructure.persistence.PostScrapRepository;
import com.community.platform.moderation.domain.UserPenalty;
import com.community.platform.moderation.infrastructure.persistence.UserPenaltyRepository;
import com.community.platform.reward.application.PointService;
import com.community.platform.reward.domain.UserPoint;
import com.community.platform.reward.infrastructure.persistence.UserPointRepository;
import com.community.platform.user.domain.User;
import com.community.platform.user.domain.UserStatus;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 기능 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserPointRepository userPointRepository;
    private final UserPenaltyRepository userPenaltyRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;
    private final PointService pointService;

    /**
     * 관리자 통계 조회
     */
    public AdminStatisticsResponse getStatistics() {
        log.info("관리자 통계 조회");

        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();

        // 오늘 신규 사용자 (임시로 0)
        long newUsersToday = 0;

        // 오늘 신규 게시글 (임시로 0)
        long newPostsToday = 0;

        // 활성 사용자 수
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);

        return AdminStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .totalPosts(totalPosts)
                .totalComments(totalComments)
                .totalReports(0L) // TODO: Report 기능 구현 시 추가
                .activeUsers(activeUsers)
                .newUsersToday(newUsersToday)
                .newPostsToday(newPostsToday)
                .pendingReports(0L) // TODO: Report 기능 구현 시 추가
                .build();
    }

    /**
     * 사용자 목록 조회 (페이징, 검색, 필터링)
     */
    public Page<UserManagementResponse> getUserList(
            String keyword,
            UserStatus status,
            UserRole role,
            Pageable pageable
    ) {
        log.info("사용자 목록 조회 - keyword: {}, status: {}, role: {}", keyword, status, role);

        Page<User> users;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색어가 있으면 닉네임 또는 이메일로 검색
            users = userRepository.findByNicknameContainingAndStatus(keyword,
                    status != null ? status : UserStatus.ACTIVE, pageable);
        } else if (status != null && role != null) {
            // 상태와 역할로 필터링 - 커스텀 쿼리 필요
            users = userRepository.findAll(pageable);
        } else if (status != null) {
            users = userRepository.findByStatus(status, pageable);
        } else if (role != null) {
            users = userRepository.findByRole(role, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::convertToUserManagementResponse);
    }

    /**
     * 사용자 상세 조회
     */
    public UserDetailResponse getUserDetail(Long userId) {
        log.info("사용자 상세 조회 - userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 포인트 정보 조회
        UserPoint userPoint = userPointRepository.findByUserId(userId).orElse(null);

        // 활동 통계
        Long totalPosts = postRepository.countByAuthorIdAndStatus(userId, PostStatus.PUBLISHED);
        Long totalComments = commentRepository.countByAuthorIdAndStatus(userId,
                com.community.platform.content.domain.CommentStatus.ACTIVE);
        Long totalLikes = postLikeRepository.countByUserId(userId);
        Long totalScraps = postScrapRepository.countByUserId(userId);

        // 제재 이력
        List<UserPenalty> penalties = userPenaltyRepository.findActiveByUserId(userId);
        List<UserDetailResponse.SanctionInfo> sanctions = penalties.stream()
                .map(penalty -> UserDetailResponse.SanctionInfo.builder()
                        .sanctionId(penalty.getId())
                        .type(penalty.getPenaltyType().name())
                        .reason(penalty.getReason())
                        .startDate(penalty.getCreatedAt())
                        .endDate(penalty.getEndDate())
                        .status(penalty.getIsActive() ? "ACTIVE" : "EXPIRED")
                        .build())
                .collect(Collectors.toList());

        return UserDetailResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .status(user.getStatus())
                .role(user.getRole())
                .currentPoints(userPoint != null ? userPoint.getAvailablePoints() : 0)
                .currentLevel(userPoint != null ? userPoint.getCurrentLevel().getLevelNumber() : 1)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .bio(user.getProfile() != null ? user.getProfile().getBio() : null)
                .avatarUrl(user.getProfile() != null ? user.getProfile().getProfileImageUrl() : null)
                .totalPosts(totalPosts)
                .totalComments(totalComments)
                .totalLikes(totalLikes)
                .totalScraps(totalScraps)
                .sanctions(sanctions)
                .build();
    }

    /**
     * 사용자 상태 변경
     */
    @Transactional
    public void updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        log.info("사용자 상태 변경 - userId: {}, status: {}", userId, request.getStatus());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        switch (request.getStatus()) {
            case ACTIVE:
                user.activate();
                break;
            case INACTIVE:
                user.deactivate();
                break;
            case BLOCKED:
                user.block(request.getReason() != null ? request.getReason() : "관리자에 의한 차단");
                break;
        }

        userRepository.save(user);
    }

    /**
     * 사용자 역할 변경
     */
    @Transactional
    public void updateUserRole(Long userId, UpdateUserRoleRequest request) {
        log.info("사용자 역할 변경 - userId: {}, role: {}", userId, request.getRole());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        user.changeRole(request.getRole());
        userRepository.save(user);
    }

    /**
     * 사용자 포인트 조정
     */
    @Transactional
    public void adjustUserPoint(Long userId, AdjustUserPointRequest request) {
        log.info("사용자 포인트 조정 - userId: {}, points: {}, reason: {}",
                userId, request.getPoints(), request.getReason());

        if (request.getPoints() > 0) {
            // 포인트 증가
            pointService.earnPoints(userId,
                com.community.platform.reward.domain.PointTransactionType.ADMIN_GRANT,
                request.getPoints(),
                null,
                request.getReason());
        } else if (request.getPoints() < 0) {
            // 포인트 감소
            pointService.earnPoints(userId,
                com.community.platform.reward.domain.PointTransactionType.ADMIN_DEDUCT,
                Math.abs(request.getPoints()),
                null,
                request.getReason());
        }
    }

    /**
     * User를 UserManagementResponse로 변환
     */
    private UserManagementResponse convertToUserManagementResponse(User user) {
        UserPoint userPoint = userPointRepository.findByUserId(user.getId()).orElse(null);
        Long totalPosts = postRepository.countByAuthorIdAndStatus(user.getId(), PostStatus.PUBLISHED);
        Long totalComments = commentRepository.countByAuthorIdAndStatus(user.getId(),
                com.community.platform.content.domain.CommentStatus.ACTIVE);

        return UserManagementResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .status(user.getStatus())
                .role(user.getRole())
                .currentPoints(userPoint != null ? userPoint.getAvailablePoints() : 0)
                .currentLevel(userPoint != null ? userPoint.getCurrentLevel().getLevelNumber() : 1)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .totalPosts(totalPosts)
                .totalComments(totalComments)
                .build();
    }

    // ========== 콘텐츠 관리 (Content Moderation) ==========

    /**
     * 게시글 목록 조회 (관리자용 - 상태별, 검색)
     */
    public Page<PostManagementResponse> getPostList(
            PostStatus status,
            String keyword,
            Boolean isNotice,
            Pageable pageable
    ) {
        log.info("게시글 목록 조회 - status: {}, keyword: {}, isNotice: {}", status, keyword, isNotice);

        Page<com.community.platform.content.domain.Post> posts;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색어가 있으면 제목 또는 내용으로 검색
            if (status != null) {
                posts = postRepository.searchByKeywordAndStatus(keyword, status, pageable);
            } else {
                // 상태 무관 검색 (전체 검색)
                posts = postRepository.findAll(pageable);
            }
        } else if (isNotice != null && isNotice) {
            // 공지사항만 조회
            List<com.community.platform.content.domain.Post> noticePosts =
                    postRepository.findNoticePostsByStatus(status != null ? status : PostStatus.PUBLISHED);
            posts = new org.springframework.data.domain.PageImpl<>(
                    noticePosts, pageable, noticePosts.size());
        } else if (status != null) {
            posts = postRepository.findByStatusOrderByPublishedAtDesc(status, pageable);
        } else {
            posts = postRepository.findAll(pageable);
        }

        return posts.map(this::convertToPostManagementResponse);
    }

    /**
     * 게시글 상태 변경
     */
    @Transactional
    public void updatePostStatus(Long postId, UpdatePostStatusRequest request, Long adminId) {
        log.info("게시글 상태 변경 - postId: {}, status: {}, adminId: {}", postId, request.getStatus(), adminId);

        com.community.platform.content.domain.Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + postId));

        switch (request.getStatus()) {
            case DELETED:
                post.delete(adminId);
                break;
            case PUBLISHED:
                if (post.getStatus() == PostStatus.DELETED) {
                    post.restore();
                } else if (post.getStatus() == PostStatus.DRAFT) {
                    post.publish();
                }
                break;
            case DRAFT:
                // DRAFT로 변경은 일반적으로 허용하지 않음 (필요시 추가)
                throw new IllegalStateException("DRAFT 상태로 변경할 수 없습니다.");
        }

        postRepository.save(post);
    }

    /**
     * 게시글 공지사항 지정/해제
     */
    @Transactional
    public void markPostAsNotice(Long postId, MarkAsNoticeRequest request) {
        log.info("게시글 공지사항 지정/해제 - postId: {}, isNotice: {}", postId, request.getIsNotice());

        com.community.platform.content.domain.Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + postId));

        if (request.getIsNotice()) {
            post.markAsNotice();
        } else {
            post.unmarkAsNotice();
        }

        postRepository.save(post);
    }

    /**
     * 게시글 일괄 처리
     */
    @Transactional
    public void bulkPostAction(BulkPostActionRequest request, Long adminId) {
        log.info("게시글 일괄 처리 - action: {}, count: {}, adminId: {}",
                request.getAction(), request.getPostIds().size(), adminId);

        List<com.community.platform.content.domain.Post> posts =
                postRepository.findAllById(request.getPostIds());

        switch (request.getAction()) {
            case DELETE:
                posts.forEach(post -> post.delete(adminId));
                break;
            case RESTORE:
                posts.forEach(com.community.platform.content.domain.Post::restore);
                break;
            case MARK_AS_NOTICE:
                posts.forEach(com.community.platform.content.domain.Post::markAsNotice);
                break;
            case UNMARK_AS_NOTICE:
                posts.forEach(com.community.platform.content.domain.Post::unmarkAsNotice);
                break;
        }

        postRepository.saveAll(posts);
    }

    /**
     * Post를 PostManagementResponse로 변환
     */
    private PostManagementResponse convertToPostManagementResponse(com.community.platform.content.domain.Post post) {
        // 작성자 정보 조회
        com.community.platform.user.domain.User author = userRepository.findById(post.getAuthorId())
                .orElse(null);

        // 스크랩 수 조회
        Long scrapCount = postScrapRepository.countByPostId(post.getId());

        return PostManagementResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .summary(post.getContent().length() > 100 ?
                        post.getContent().substring(0, 100) + "..." : post.getContent())
                .authorNickname(author != null ? author.getNickname() : "알 수 없음")
                .authorId(post.getAuthorId())
                .categoryName(post.getCategory() != null ? post.getCategory().getName() : "없음")
                .status(post.getStatus())
                .isNoticePost(post.getIsNoticePost())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .scrapCount(scrapCount)
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .deletedAt(post.getDeletedAt())
                .deletedBy(post.getDeletedBy())
                .build();
    }
}
