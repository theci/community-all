'use client';

import useSWR from 'swr';
import { commentService } from '../services';
import type { Comment } from '../types';

export function useComments(postId: number | null) {
  const { data, error, isLoading, mutate } = useSWR<Comment[]>(
    postId ? `/comments/posts/${postId}` : null,
    postId ? () => commentService.getCommentsByPost(postId) : null
  );

  return {
    comments: data || [],
    isLoading,
    isError: error,
    mutate,
  };
}

export function useRootComments(postId: number | null, page = 0, size = 20) {
  const { data, error, isLoading, mutate } = useSWR<Comment[]>(
    postId ? `/comments/posts/${postId}/root?page=${page}&size=${size}` : null,
    postId ? () => commentService.getRootComments(postId, page, size) : null
  );

  return {
    comments: data || [],
    isLoading,
    isError: error,
    mutate,
  };
}

export function useReplies(parentCommentId: number | null) {
  const { data, error, isLoading, mutate } = useSWR<Comment[]>(
    parentCommentId ? `/comments/${parentCommentId}/replies` : null,
    parentCommentId ? () => commentService.getReplies(parentCommentId) : null
  );

  return {
    replies: data || [],
    isLoading,
    isError: error,
    mutate,
  };
}
