'use client';

import useSWR from 'swr';
import { postService } from '../services';
import type { Post, PostListResponse, PostSearchRequest } from '../types';

export function usePosts(page = 0, size = 20, sort = 'createdAt,desc') {
  const { data, error, isLoading, mutate } = useSWR<PostListResponse>(
    `/posts?page=${page}&size=${size}&sort=${sort}`,
    () => postService.getPosts(page, size, sort),
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: false,
    }
  );

  return {
    posts: data?.content || [],
    totalPages: data?.pageInfo?.totalPages || 0,
    totalElements: data?.pageInfo?.totalElements || 0,
    currentPage: data?.pageInfo?.currentPage || 0,
    isLoading,
    isError: error,
    mutate,
  };
}

export function usePost(postId: number | null) {
  const { data, error, isLoading, mutate } = useSWR<Post>(
    postId ? `/posts/${postId}` : null,
    postId ? () => postService.getPost(postId) : null
  );

  return {
    post: data,
    isLoading,
    isError: error,
    mutate,
  };
}

export function useSearchPosts(params: PostSearchRequest, page = 0, size = 20) {
  const { data, error, isLoading, mutate } = useSWR<PostListResponse>(
    params.keyword || params.categoryId ? ['/posts/search', params, page, size] : null,
    () => postService.searchPosts(params, page, size)
  );

  return {
    posts: data?.content || [],
    totalPages: data?.pageInfo?.totalPages || 0,
    totalElements: data?.pageInfo?.totalElements || 0,
    isLoading,
    isError: error,
    mutate,
  };
}

export function usePopularPosts(limit = 10) {
  const { data, error, isLoading } = useSWR<Post[]>(
    `/posts/popular?limit=${limit}`,
    () => postService.getPopularPosts(limit),
    {
      revalidateOnFocus: false,
      dedupingInterval: 60000, // 1분
    }
  );

  return {
    posts: data || [],
    isLoading,
    isError: error,
  };
}

export function useTrendingPosts(days = 7) {
  const { data, error, isLoading } = useSWR<Post[]>(
    `/posts/trending?days=${days}`,
    () => postService.getTrendingPosts(days),
    {
      revalidateOnFocus: false,
      dedupingInterval: 60000, // 1분
    }
  );

  return {
    posts: data || [],
    isLoading,
    isError: error,
  };
}
