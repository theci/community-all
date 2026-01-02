package com.community.platform.content.exception;

import com.community.platform.shared.domain.BusinessException;

/**
 * 댓글을 찾을 수 없을 때 발생하는 예외
 */
public class CommentNotFoundException extends BusinessException {
    
    public CommentNotFoundException(Long commentId) {
        super("COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다. ID: " + commentId);
    }
}