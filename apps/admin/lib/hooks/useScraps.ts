'use client';

import useSWR from 'swr';
import { scrapService } from '../services';
import { useAuth } from './useAuth';
import type { ScrapFolder, PostScrap } from '../types';

export function useScrapFolders() {
  const { user } = useAuth();
  const userId = user?.id;

  const { data, error, isLoading, mutate } = useSWR<ScrapFolder[]>(
    userId ? ['/scrap-folders/me', userId] : null,
    userId ? () => scrapService.getMyFolders(userId) : null
  );

  return {
    folders: data || [],
    isLoading,
    isError: error,
    mutate,
  };
}

export function useScrapFolder(folderId: number | null) {
  const { user } = useAuth();
  const userId = user?.id;

  const { data, error, isLoading, mutate } = useSWR<ScrapFolder>(
    folderId && userId ? [`/scrap-folders/${folderId}`, userId] : null,
    folderId && userId ? () => scrapService.getFolder(folderId, userId) : null
  );

  return {
    folder: data,
    isLoading,
    isError: error,
    mutate,
  };
}

export function useScrapsInFolder(folderId: number | null) {
  const { user } = useAuth();
  const userId = user?.id;

  const { data, error, isLoading, mutate } = useSWR<PostScrap[]>(
    folderId && userId ? [`/posts/scrap-folders/${folderId}/scraps`, userId] : null,
    folderId && userId ? () => scrapService.getScrapsInFolder(folderId, userId) : null
  );

  return {
    scraps: data || [],
    isLoading,
    isError: error,
    mutate,
  };
}

export function useMyScraps() {
  const { user } = useAuth();
  const userId = user?.id;

  const { data, error, isLoading, mutate } = useSWR<PostScrap[]>(
    userId ? ['/posts/scraps/me', userId] : null,
    userId ? () => scrapService.getMyScraps(userId) : null
  );

  return {
    scraps: data || [],
    isLoading,
    isError: error,
    mutate,
  };
}
