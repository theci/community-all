import { apiClient } from '../api';
import type { Category } from '../types';

interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
  message?: string;
}

export const categoryService = {
  // 전체 카테고리 트리 조회
  getCategoryTree: async (): Promise<Category[]> => {
    const response = await apiClient.get<ApiResponse<Category[]>>('/categories/tree');
    return response.data.data;
  },

  // 카테고리 목록 조회 (최상위 카테고리)
  getCategories: async (): Promise<Category[]> => {
    const response = await apiClient.get<ApiResponse<Category[]>>('/categories/root');
    return response.data.data;
  },

  // 특정 카테고리 조회
  getCategory: async (categoryId: number): Promise<Category> => {
    const response = await apiClient.get<ApiResponse<Category>>(`/categories/${categoryId}`);
    return response.data.data;
  },
};
