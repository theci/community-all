'use client';

import { useState, useEffect } from 'react';
import { Comment } from '@/lib/types';
import { CommentForm } from './CommentForm';
import { Button } from '@/components/ui';

interface CommentItemProps {
  comment: Comment;
  currentUserId?: number;
  depth?: number; // 댓글 깊이 (0: 최상위, 1-5: 대댓글 레벨)
  onReply: (parentId: number, content: string) => Promise<void>;
  onEdit: (commentId: number, content: string) => Promise<void>;
  onDelete: (commentId: number) => Promise<void>;
  onLoadReplies?: (commentId: number) => Promise<void>;
}

export function CommentItem({
  comment,
  currentUserId,
  depth = 0,
  onReply,
  onEdit,
  onDelete,
  onLoadReplies,
}: CommentItemProps) {
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [showReplies, setShowReplies] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const isAuthor = currentUserId === comment.author.id;
  const isDeleted = comment.status === 'DELETED';

  // 답글이 있으면 자동으로 펼치기
  useEffect(() => {
    if (comment.replies && comment.replies.length > 0) {
      setShowReplies(true);
    }
  }, [comment.replies]);

  // 최대 5단계까지 들여쓰기, 이후는 5단계 레벨 유지
  const currentDepth = Math.min(depth, 5);
  const nextDepth = Math.min(depth + 1, 5);

  // depth 5 미만일 때만 들여쓰기 추가 (5 이상은 더 이상 들여쓰기 안 함)
  const shouldIndent = depth > 0 && depth < 5;

  // depth에 따른 배경색 (깊이 구분)
  const bgColorClass = currentDepth === 0
    ? 'bg-gray-50'
    : currentDepth === 1
    ? 'bg-blue-50'
    : currentDepth === 2
    ? 'bg-green-50'
    : currentDepth === 3
    ? 'bg-yellow-50'
    : currentDepth === 4
    ? 'bg-purple-50'
    : 'bg-pink-50';

  const handleReply = async (content: string) => {
    await onReply(comment.id, content);
    setShowReplyForm(false);
  };

  const handleEdit = async (content: string) => {
    await onEdit(comment.id, content);
    setIsEditing(false);
  };

  const handleDelete = async () => {
    if (!confirm('댓글을 삭제하시겠습니까?')) return;

    setIsDeleting(true);
    try {
      await onDelete(comment.id);
    } finally {
      setIsDeleting(false);
    }
  };

  const handleToggleReplies = async () => {
    if (!showReplies && onLoadReplies && comment.replyCount > 0) {
      await onLoadReplies(comment.id);
    }
    setShowReplies(!showReplies);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (minutes < 1) return '방금 전';
    if (minutes < 60) return `${minutes}분 전`;
    if (hours < 24) return `${hours}시간 전`;
    if (days < 7) return `${days}일 전`;

    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  return (
    <div className={`comment-item ${shouldIndent ? 'pl-6 border-l-2 border-gray-200' : ''}`}>
      <div className="flex gap-3">
        {/* 프로필 이미지 */}
        <div className="flex-shrink-0">
          <div className={`${currentDepth > 0 ? 'w-8 h-8' : 'w-10 h-10'} rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white font-medium text-sm`}>
            {comment.author.nickname?.[0] || comment.author.username[0]}
          </div>
        </div>

        {/* 댓글 내용 */}
        <div className="flex-1">
          <div className={`${bgColorClass} rounded-lg p-3 ${currentDepth > 0 ? 'border border-gray-200' : ''}`}>
            {/* 작성자 정보 */}
            <div className="flex items-center gap-2 mb-2">
              {currentDepth > 0 && (
                <span className="text-xs font-semibold text-blue-600">↳</span>
              )}
              <span className={`font-medium text-gray-900 ${currentDepth > 0 ? 'text-sm' : ''}`}>
                {comment.author.nickname || comment.author.username}
              </span>
              <span className="text-xs text-gray-500">{formatDate(comment.createdAt)}</span>
              {comment.updatedAt !== comment.createdAt && (
                <span className="text-xs text-gray-500">(수정됨)</span>
              )}
            </div>

            {/* 댓글 본문 */}
            {isDeleted ? (
              <p className="text-gray-500 italic">삭제된 댓글입니다.</p>
            ) : isEditing ? (
              <CommentForm
                onSubmit={handleEdit}
                onCancel={() => setIsEditing(false)}
                initialValue={comment.content}
                placeholder="댓글을 수정하세요"
                submitLabel="수정"
              />
            ) : (
              <p className="text-gray-800 whitespace-pre-wrap">{comment.content}</p>
            )}
          </div>

          {/* 액션 버튼 */}
          {!isDeleted && (
            <div className="flex items-center gap-3 mt-2 text-sm">
              <button
                onClick={() => setShowReplyForm(!showReplyForm)}
                className="text-gray-600 hover:text-blue-600 font-medium"
              >
                답글
              </button>

              {isAuthor && !isEditing && (
                <>
                  <button
                    onClick={() => setIsEditing(true)}
                    className="text-gray-600 hover:text-blue-600 font-medium"
                  >
                    수정
                  </button>
                  <button
                    onClick={handleDelete}
                    disabled={isDeleting}
                    className="text-gray-600 hover:text-red-600 font-medium disabled:opacity-50"
                  >
                    {isDeleting ? '삭제 중...' : '삭제'}
                  </button>
                </>
              )}

              {comment.replyCount > 0 && (
                <button
                  onClick={handleToggleReplies}
                  className="text-blue-600 hover:text-blue-700 font-medium"
                >
                  {showReplies ? '답글 숨기기' : `답글 ${comment.replyCount}개 보기`}
                </button>
              )}
            </div>
          )}

          {/* 답글 작성 폼 */}
          {showReplyForm && !isDeleted && (
            <div className="mt-3">
              <CommentForm
                onSubmit={handleReply}
                onCancel={() => setShowReplyForm(false)}
                placeholder="답글을 작성하세요"
                submitLabel="답글 작성"
              />
            </div>
          )}

          {/* 답글 목록 */}
          {showReplies && comment.replies && comment.replies.length > 0 && (
            <div className="mt-3 space-y-3">
              {comment.replies.map((reply) => (
                <CommentItem
                  key={reply.id}
                  comment={reply}
                  currentUserId={currentUserId}
                  depth={nextDepth}
                  onReply={onReply}
                  onEdit={onEdit}
                  onDelete={onDelete}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
