'use client';

import { useState, useEffect, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { postService } from '@/lib/services';
import { Input, Select, Button, Badge } from '@/components/ui';
import type { Post, Category } from '@/lib/types';

const SEARCH_HISTORY_KEY = 'search_history';
const MAX_HISTORY = 10;

function SearchPageContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [searchQuery, setSearchQuery] = useState(searchParams.get('q') || '');
  const [category, setCategory] = useState(searchParams.get('category') || '');
  const [sortBy, setSortBy] = useState(searchParams.get('sort') || 'latest');
  const [startDate, setStartDate] = useState(searchParams.get('startDate') || '');
  const [endDate, setEndDate] = useState(searchParams.get('endDate') || '');

  const [results, setResults] = useState<Post[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [searchHistory, setSearchHistory] = useState<string[]>([]);

  // 임시 카테고리 데이터
  const categories: Category[] = [
    { id: 1, name: '공지사항', slug: 'notice', description: '' },
    { id: 2, name: '자유게시판', slug: 'free', description: '' },
    { id: 3, name: '질문답변', slug: 'qna', description: '' },
    { id: 4, name: '개발', slug: 'dev', description: '' },
    { id: 5, name: '디자인', slug: 'design', description: '' },
  ];

  useEffect(() => {
    loadSearchHistory();

    // URL 파라미터가 있으면 자동 검색
    const query = searchParams.get('q');
    if (query) {
      handleSearch(query);
    }
  }, []);

  const loadSearchHistory = () => {
    try {
      const history = localStorage.getItem(SEARCH_HISTORY_KEY);
      if (history) {
        setSearchHistory(JSON.parse(history));
      }
    } catch (err) {
      console.error('Failed to load search history:', err);
    }
  };

  const saveSearchHistory = (query: string) => {
    if (!query.trim()) return;

    try {
      const history = [...searchHistory];
      const index = history.indexOf(query);

      if (index > -1) {
        history.splice(index, 1);
      }

      history.unshift(query);

      if (history.length > MAX_HISTORY) {
        history.pop();
      }

      setSearchHistory(history);
      localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(history));
    } catch (err) {
      console.error('Failed to save search history:', err);
    }
  };

  const clearSearchHistory = () => {
    setSearchHistory([]);
    localStorage.removeItem(SEARCH_HISTORY_KEY);
  };

  const handleSearch = async (query?: string) => {
    const searchTerm = query || searchQuery;

    if (!searchTerm.trim()) return;

    try {
      setLoading(true);
      setSearched(true);

      // 검색 기록 저장
      saveSearchHistory(searchTerm);

      // URL 업데이트
      const params = new URLSearchParams();
      params.set('q', searchTerm);
      if (category) params.set('category', category);
      if (sortBy) params.set('sort', sortBy);
      if (startDate) params.set('startDate', startDate);
      if (endDate) params.set('endDate', endDate);

      router.push(`/search?${params.toString()}`);

      // 검색 API 호출
      const searchParams: any = {
        keyword: searchTerm,
      };

      if (category) {
        searchParams.categoryId = parseInt(category);
      }

      const response = await postService.searchPosts(searchParams, 0, 20);
      setResults(Array.isArray(response.content) ? response.content : []);
    } catch (err) {
      console.error('Search failed:', err);
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const highlightText = (text: string, query: string) => {
    if (!query.trim()) return text;

    const parts = text.split(new RegExp(`(${query})`, 'gi'));
    return parts.map((part, index) =>
      part.toLowerCase() === query.toLowerCase() ? (
        <mark key={index} className="bg-yellow-200 text-gray-900 px-1 rounded">
          {part}
        </mark>
      ) : (
        part
      )
    );
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 py-8">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* 검색 헤더 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">고급 검색</h1>

          {/* 검색 입력 */}
          <div className="mb-4">
            <Input
              placeholder="검색어를 입력하세요..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={handleKeyPress}
              fullWidth
            />
          </div>

          {/* 필터 옵션 */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
            <Select
              placeholder="카테고리 선택"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              options={[
                { value: '', label: '전체 카테고리' },
                ...categories.map(cat => ({ value: cat.id.toString(), label: cat.name }))
              ]}
            />

            <Select
              placeholder="정렬"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              options={[
                { value: 'latest', label: '최신순' },
                { value: 'popular', label: '인기순' },
                { value: 'views', label: '조회순' },
              ]}
            />

            <div className="flex gap-2">
              <Input
                type="date"
                placeholder="시작일"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
              <Input
                type="date"
                placeholder="종료일"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>
          </div>

          {/* 검색 버튼 */}
          <Button
            onClick={() => handleSearch()}
            isLoading={loading}
            fullWidth
          >
            검색
          </Button>
        </div>

        {/* 검색 기록 */}
        {!searched && searchHistory.length > 0 && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white">최근 검색어</h2>
              <button
                onClick={clearSearchHistory}
                className="text-sm text-gray-500 hover:text-gray-700"
              >
                전체 삭제
              </button>
            </div>

            <div className="flex flex-wrap gap-2">
              {searchHistory.map((term, index) => (
                <button
                  key={index}
                  onClick={() => {
                    setSearchQuery(term);
                    handleSearch(term);
                  }}
                  className="px-3 py-1 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-full text-sm transition-colors"
                >
                  {term}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* 검색 결과 */}
        {searched && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                검색 결과 {results.length > 0 && `(${results.length})`}
              </h2>
            </div>

            {loading ? (
              <div className="space-y-4">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="animate-pulse">
                    <div className="h-6 bg-gray-200 rounded w-3/4 mb-3"></div>
                    <div className="h-4 bg-gray-200 rounded w-full mb-2"></div>
                    <div className="h-4 bg-gray-200 rounded w-2/3"></div>
                  </div>
                ))}
              </div>
            ) : results.length > 0 ? (
              <div className="space-y-6">
                {results.map((post) => (
                  <Link
                    key={post.id}
                    href={`/posts/${post.id}`}
                    className="block border-b border-gray-200 pb-6 last:border-0 hover:bg-gray-50 transition-colors p-4 rounded-lg"
                  >
                    <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
                      {highlightText(post.title, searchQuery)}
                    </h3>

                    {post.summary && (
                      <p className="text-gray-600 mb-3 line-clamp-2">
                        {highlightText(post.summary, searchQuery)}
                      </p>
                    )}

                    <div className="flex items-center gap-4 text-sm text-gray-500">
                      <span>작성자: {post.author.nickname || '알 수 없음'}</span>
                      <span>👁️ {post.viewCount}</span>
                      <span>❤️ {post.likeCount}</span>
                      <span>💬 {post.commentCount}</span>
                      <span>{new Date(post.createdAt).toLocaleDateString()}</span>
                    </div>

                    {post.tags && post.tags.length > 0 && (
                      <div className="flex flex-wrap gap-2 mt-3">
                        {post.tags.map((tag) => (
                          <Badge key={tag.id} variant="primary" size="sm">
                            #{tag.name}
                          </Badge>
                        ))}
                      </div>
                    )}
                  </Link>
                ))}
              </div>
            ) : (
              <div className="text-center py-12">
                <svg
                  className="mx-auto h-12 w-12 text-gray-400 mb-4"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
                <p className="text-gray-500 dark:text-gray-400">검색 결과가 없습니다.</p>
                <p className="text-sm text-gray-400 mt-2">
                  다른 검색어나 필터를 시도해보세요.
                </p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default function SearchPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div></div>}>
      <SearchPageContent />
    </Suspense>
  );
}
