'use client';

import { useState, useEffect } from 'react';
import { Comment } from '@/lib/types';
import { commentService } from '@/lib/services';
import { CommentItem } from './CommentItem';
import { CommentForm } from './CommentForm';
import { useAuth } from '@/lib/hooks';

interface CommentListProps {
  postId: number;
}

export function CommentList({ postId }: CommentListProps) {
  const { user, isAuthenticated } = useAuth();
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadComments();
  }, [postId]);

  const loadComments = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await commentService.getCommentsByPost(postId);
      setComments(data);
    } catch (err: any) {
      console.error('Failed to load comments:', err);
      setError(err.response?.data?.message || '댓글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateComment = async (content: string) => {
    if (!user?.id) {
      alert('로그인이 필요합니다.');
      return;
    }

    try {
      const newComment = await commentService.createComment(
        {
          postId,
          content,
        },
        user.id
      );
      setComments([...comments, newComment]);
    } catch (err: any) {
      console.error('Failed to create comment:', err);
      throw err;
    }
  };

  const handleReply = async (parentId: number, content: string) => {
    if (!user?.id) {
      alert('로그인이 필요합니다.');
      return;
    }

    try {
      const newReply = await commentService.createComment(
        {
          postId,
          parentId,
          content,
        },
        user.id
      );

      // 새 답글에 replies 배열 초기화 (백엔드에서 없을 수 있음)
      const replyWithReplies = {
        ...newReply,
        replies: newReply.replies || [],
        replyCount: newReply.replyCount || 0,
      };

      // 답글을 부모 댓글의 replies 배열에 추가
      const updateCommentsWithReply = (commentList: Comment[]): Comment[] => {
        return commentList.map((comment) => {
          if (comment.id === parentId) {
            return {
              ...comment,
              replies: [...(comment.replies || []), replyWithReplies],
              replyCount: comment.replyCount + 1,
            };
          }
          if (comment.replies) {
            return {
              ...comment,
              replies: updateCommentsWithReply(comment.replies),
            };
          }
          return comment;
        });
      };

      setComments(updateCommentsWithReply(comments));
    } catch (err: any) {
      console.error('Failed to create reply:', err);
      throw err;
    }
  };

  const handleEdit = async (commentId: number, content: string) => {
    if (!user?.id) {
      alert('로그인이 필요합니다.');
      return;
    }

    try {
      await commentService.updateComment(commentId, { content }, user.id);

      // 댓글 목록에서 수정된 댓글 업데이트
      const updateComment = (commentList: Comment[]): Comment[] => {
        return commentList.map((comment) => {
          if (comment.id === commentId) {
            return { ...comment, content, updatedAt: new Date().toISOString() };
          }
          if (comment.replies) {
            return { ...comment, replies: updateComment(comment.replies) };
          }
          return comment;
        });
      };

      setComments(updateComment(comments));
    } catch (err: any) {
      console.error('Failed to update comment:', err);
      throw err;
    }
  };

  const handleDelete = async (commentId: number) => {
    if (!user?.id) {
      alert('로그인이 필요합니다.');
      return;
    }

    try {
      await commentService.deleteComment(commentId, user.id);

      // 댓글 목록에서 삭제된 댓글 상태 업데이트
      const updateComment = (commentList: Comment[]): Comment[] => {
        return commentList.map((comment) => {
          if (comment.id === commentId) {
            return { ...comment, status: 'DELETED' as any, content: '' };
          }
          if (comment.replies) {
            return { ...comment, replies: updateComment(comment.replies) };
          }
          return comment;
        });
      };

      setComments(updateComment(comments));
    } catch (err: any) {
      console.error('Failed to delete comment:', err);
      throw err;
    }
  };

  const handleLoadReplies = async (commentId: number) => {
    try {
      const replies = await commentService.getReplies(commentId);

      const updateComment = (commentList: Comment[]): Comment[] => {
        return commentList.map((comment) => {
          if (comment.id === commentId) {
            return { ...comment, replies };
          }
          if (comment.replies) {
            return { ...comment, replies: updateComment(comment.replies) };
          }
          return comment;
        });
      };

      setComments(updateComment(comments));
    } catch (err: any) {
      console.error('Failed to load replies:', err);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 댓글 작성 폼 */}
      <div>
        <h3 className="text-lg font-semibold text-gray-900 mb-4">
          댓글 {comments.length}개
        </h3>
        {isAuthenticated ? (
          <CommentForm onSubmit={handleCreateComment} submitLabel="댓글 작성" />
        ) : (
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 text-center text-gray-600">
            로그인 후 댓글을 작성할 수 있습니다.
          </div>
        )}
      </div>

      {/* 댓글 목록 */}
      <div className="space-y-4">
        {comments.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            첫 댓글을 작성해보세요!
          </div>
        ) : (
          comments.map((comment) => (
            <CommentItem
              key={comment.id}
              comment={comment}
              currentUserId={user?.id}
              depth={0}
              onReply={handleReply}
              onEdit={handleEdit}
              onDelete={handleDelete}
              onLoadReplies={handleLoadReplies}
            />
          ))
        )}
      </div>
    </div>
  );
}
