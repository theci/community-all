package com.community.platform.content.application;

import com.community.platform.content.domain.*;
import com.community.platform.content.exception.CommentNotFoundException;
import com.community.platform.content.exception.PostNotFoundException;
import com.community.platform.content.infrastructure.persistence.CommentRepository;
import com.community.platform.content.infrastructure.persistence.PostRepository;
import com.community.platform.moderation.application.UserPenaltyService;
import com.community.platform.moderation.exception.UserPenaltyException;
import com.community.platform.shared.infrastructure.DomainEventPublisher;
import com.community.platform.user.exception.UserNotFoundException;
import com.community.platform.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 댓글 관리 애플리케이션 서비스
 * 댓글 및 대댓글 생명주기와 관련된 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserPenaltyService penaltyService;
    private final DomainEventPublisher eventPublisher;

    /**
     * 새 댓글 작성
     */
    @Transactional
    public Comment createComment(Long postId, Long authorId, String content) {
        log.info("댓글 작성 시작. postId: {}, authorId: {}", postId, authorId);

        // 게시글 존재 확인
        Post post = getPostById(postId);

        // 작성자 존재 확인
        validateUserExists(authorId);

        // 제재 체크 추가
        if (penaltyService.hasCommentBan(authorId)) {
            log.warn("댓글 제재 중인 사용자의 댓글 작성 시도. userId: {}", authorId);
            throw new UserPenaltyException("댓글 작성 제재 중입니다. 댓글을 작성할 수 없습니다.");
        }

        // 댓글 생성
        Comment comment = Comment.create(post, authorId, content);
        Comment savedComment = commentRepository.save(comment);

        // 도메인 이벤트 발행
        eventPublisher.publishEvents(savedComment);

        log.info("댓글 작성 완료. commentId: {}", savedComment.getId());
        return savedComment;
    }

    /**
     * 대댓글 작성
     */
    @Transactional
    public Comment createReply(Long postId, Long parentCommentId, Long authorId, String content) {
        log.info("대댓글 작성 시작. postId: {}, parentCommentId: {}, authorId: {}",
                postId, parentCommentId, authorId);

        // 게시글 존재 확인
        Post post = getPostById(postId);

        // 부모 댓글 존재 확인
        Comment parentComment = getCommentById(parentCommentId);

        // 작성자 존재 확인
        validateUserExists(authorId);

        // 제재 체크 추가
        if (penaltyService.hasCommentBan(authorId)) {
            log.warn("댓글 제재 중인 사용자의 대댓글 작성 시도. userId: {}", authorId);
            throw new UserPenaltyException("댓글 작성 제재 중입니다. 대댓글을 작성할 수 없습니다.");
        }

        // 대댓글 생성
        Comment reply = Comment.createReply(post, parentComment, authorId, content);
        Comment savedReply = commentRepository.save(reply);

        // 도메인 이벤트 발행
        eventPublisher.publishEvents(savedReply);

        log.info("대댓글 작성 완료. replyId: {}", savedReply.getId());
        return savedReply;
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public void updateComment(Long commentId, Long authorId, String content) {
        log.info("댓글 수정 처리. commentId: {}, authorId: {}", commentId, authorId);
        
        Comment comment = getCommentById(commentId);
        
        // 작성자 권한 확인
        validateAuthorPermission(comment, authorId);
        
        // 댓글 내용 수정
        comment.updateContent(content);
        
        log.info("댓글 수정 완료. commentId: {}", commentId);
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteComment(Long commentId, Long authorId) {
        log.info("댓글 삭제 처리. commentId: {}, authorId: {}", commentId, authorId);
        
        Comment comment = getCommentById(commentId);
        
        // 작성자 권한 확인
        validateAuthorPermission(comment, authorId);
        
        // 댓글 삭제 (자동으로 게시글 댓글 수 감소)
        comment.delete();
        
        log.info("댓글 삭제 완료. commentId: {}", commentId);
    }

    /**
     * 댓글 차단 (관리자 기능)
     */
    @Transactional
    public void blockComment(Long commentId) {
        log.info("댓글 차단 처리. commentId: {}", commentId);
        
        Comment comment = getCommentById(commentId);
        comment.block();
        
        log.info("댓글 차단 완료. commentId: {}", commentId);
    }

    /**
     * 댓글 복원 (관리자 기능)
     */
    @Transactional
    public void restoreComment(Long commentId) {
        log.info("댓글 복원 처리. commentId: {}", commentId);
        
        Comment comment = getCommentById(commentId);
        comment.restore();
        
        log.info("댓글 복원 완료. commentId: {}", commentId);
    }

    /**
     * 댓글 조회
     */
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    /**
     * 게시글의 댓글 목록 조회 (계층형 구조)
     */
    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdWithHierarchy(postId, CommentStatus.ACTIVE);
    }

    /**
     * 게시글의 최상위 댓글만 조회
     */
    public List<Comment> getRootCommentsByPost(Long postId) {
        return commentRepository.findRootCommentsByPostIdAndStatus(postId, CommentStatus.ACTIVE);
    }

    /**
     * 특정 댓글의 대댓글 목록 조회
     */
    public List<Comment> getRepliesByParentComment(Long parentCommentId) {
        return commentRepository.findByParentCommentIdAndStatusOrderByCreatedAt(
                parentCommentId, CommentStatus.ACTIVE);
    }

    /**
     * 작성자별 댓글 목록 조회
     */
    public Page<Comment> getCommentsByAuthor(Long authorId, Pageable pageable) {
        return commentRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(
                authorId, CommentStatus.ACTIVE, pageable);
    }

    /**
     * 댓글 검색 (내용 기반)
     */
    public Page<Comment> searchComments(String keyword, Pageable pageable) {
        return commentRepository.findByContentContainingAndStatus(
                keyword, CommentStatus.ACTIVE, pageable);
    }

    /**
     * 최근 댓글 조회 (관리자용)
     */
    public Page<Comment> getRecentComments(Pageable pageable) {
        return commentRepository.findRecentCommentsByStatus(CommentStatus.ACTIVE, pageable);
    }

    /**
     * 게시글의 댓글 수 조회
     */
    public Long getCommentCountByPost(Long postId) {
        return commentRepository.countByPostIdAndStatus(postId, CommentStatus.ACTIVE);
    }

    /**
     * 작성자별 댓글 수 조회
     */
    public Long getCommentCountByAuthor(Long authorId) {
        return commentRepository.countByAuthorIdAndStatus(authorId, CommentStatus.ACTIVE);
    }

    /**
     * 게시글 조회 (존재 확인용)
     */
    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
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
     * 작성자 권한 확인
     */
    private void validateAuthorPermission(Comment comment, Long userId) {
        if (!comment.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정/삭제할 수 있습니다.");
        }
    }
}