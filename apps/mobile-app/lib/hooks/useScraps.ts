'use client';

import useSWR from 'swr';
import { scrapService } from '../services';
import { useAuthStore } from '../store/authStore';
import type { ScrapFolder, PostScrap } from '../types';

export function useScrapFolders() {
  const { data, error, isLoading, mutate } = useSWR<ScrapFolder[]>(
    '/scrap-folders/me',
    scrapService.getMyFolders
  );

  return {
    folders: data || [],
    isLoading,
    isError: error,
    mutate,
  };
}

export function useScrapFolder(folderId: number | null) {
  const user = useAuthStore((state) => state.user);
  const currentUserId = user?.id || 0;

  const { data, error, isLoading, mutate } = useSWR<ScrapFolder>(
    folderId ? `/scrap-folders/${folderId}` : null,
    folderId ? () => scrapService.getFolder(folderId, currentUserId) : null
  );

  return {
    folder: data,
    isLoading,
    isError: error,
    mutate,
  };
}

export function useScrapsInFolder(folderId: number | null) {
  const user = useAuthStore((state) => state.user);
  const currentUserId = user?.id || 0;

  const { data, error, isLoading, mutate } = useSWR<PostScrap[]>(
    folderId ? `/posts/scrap-folders/${folderId}/scraps` : null,
    folderId ? () => scrapService.getScrapsInFolder(folderId, currentUserId) : null
  );

  return {
    scraps: data || [],
    isLoading,
    isError: error,
    mutate,
  };
}

export function useMyScraps() {
  const { data, error, isLoading, mutate } = useSWR<PostScrap[]>(
    '/posts/scraps/me',
    scrapService.getMyScraps
  );

  return {
    scraps: data || [],
    isLoading,
    isError: error,
    mutate,
  };
}
