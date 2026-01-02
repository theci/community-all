'use client';

import useSWR from 'swr';
import { pointService } from '../services';
import type { PointInfo, PointTransaction, PointRanking } from '../types';

export function useMyPoints() {
  const { data, error, isLoading, mutate } = useSWR<PointInfo>(
    '/points/me',
    pointService.getMyPoints
  );

  return {
    points: data,
    isLoading,
    isError: error,
    mutate,
  };
}

export function useMyTransactions(page = 0, size = 20) {
  const { data, error, isLoading } = useSWR<PointTransaction[]>(
    `/points/me/transactions?page=${page}&size=${size}`,
    () => pointService.getMyTransactions(page, size)
  );

  return {
    transactions: data || [],
    isLoading,
    isError: error,
  };
}

export function usePointRanking(limit = 100) {
  const { data, error, isLoading } = useSWR<PointRanking[]>(
    `/points/ranking?limit=${limit}`,
    () => pointService.getRanking(limit),
    {
      revalidateOnFocus: false,
      dedupingInterval: 300000, // 5분
    }
  );

  return {
    ranking: data || [],
    isLoading,
    isError: error,
  };
}
