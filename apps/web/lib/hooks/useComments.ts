'use client';

import useSWR from 'swr';
import { commentService } from '../services';
import type { Comment } from '../types';

const fetcher = (url: string, id: number, method: string) => {
  if (method === 'getCommentsByPost') {
    return commentService.getCommentsByPost(id);
  } else if (method === 'getReplies') {
    return commentService.getReplies(id);
  }
  return Promise.resolve([]);
};

const rootFetcher = (url: string, id: number, page: number, size: number) => {
  return commentService.getRootComments(id, page, size);
};

export function useComments(postId: number | null) {
  const { data, error, isLoading, mutate } = useSWR<Comment[]>(
    postId ? ['/comments/posts', postId, 'getCommentsByPost'] : null,
    ([url, id, method]) => fetcher(url, id, method)
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
    postId ? ['/comments/posts/root', postId, page, size] : null,
    ([url, id, p, s]) => rootFetcher(url, id, p, s)
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
    parentCommentId ? ['/comments/replies', parentCommentId, 'getReplies'] : null,
    ([url, id, method]) => fetcher(url, id, method)
  );

  return {
    replies: data || [],
    isLoading,
    isError: error,
    mutate,
  };
}
