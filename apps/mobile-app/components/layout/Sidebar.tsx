'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { postService } from '@/lib/services';
import type { Post, Category } from '@/lib/types';

export default function Sidebar() {
  const [popularPosts, setPopularPosts] = useState<Post[]>([]);
  const [trendingPosts, setTrendingPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSidebarData();
  }, []);

  const loadSidebarData = async () => {
    try {
      setLoading(true);

      // 인기 게시글과 트렌딩 게시글 병렬 로딩
      const [popular, trending] = await Promise.all([
        postService.getPopularPosts(5),
        postService.getTrendingPosts(7),
      ]);

      // 배열인지 확인 후 slice
      setPopularPosts(Array.isArray(popular) ? popular.slice(0, 5) : []);
      setTrendingPosts(Array.isArray(trending) ? trending.slice(0, 5) : []);
    } catch (err) {
      console.error('Failed to load sidebar data:', err);
    } finally {
      setLoading(false);
    }
  };

  // 임시 카테고리 데이터 (추후 API 연동)
  const categories: Category[] = [
    { id: 1, name: '공지사항', slug: 'notice', description: '공지사항' },
    { id: 2, name: '자유게시판', slug: 'free', description: '자유게시판' },
    { id: 3, name: '질문답변', slug: 'qna', description: '질문답변' },
    { id: 4, name: '개발', slug: 'dev', description: '개발' },
    { id: 5, name: '디자인', slug: 'design', description: '디자인' },
  ];

  // 인기 태그 (추후 API 연동)
  const popularTags = [
    'JavaScript',
    'React',
    'TypeScript',
    'Next.js',
    'CSS',
    'Node.js',
    'Python',
    'Spring',
  ];

  return (
    <aside className="w-80 hidden lg:block">
      <div className="space-y-6 sticky top-20">
        {/* 카테고리 */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">카테고리</h3>
          <nav className="space-y-2">
            {categories.map((category) => (
              <Link
                key={category.id}
                href={`/posts?category=${category.id}`}
                className="flex items-center justify-between px-3 py-2 text-gray-700 dark:text-gray-300 hover:bg-blue-50 dark:hover:bg-blue-900/20 hover:text-blue-600 dark:hover:text-blue-400 rounded-lg transition-colors group"
              >
                <span>{category.name}</span>
                <svg
                  className="w-4 h-4 text-gray-400 dark:text-gray-500 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 5l7 7-7 7"
                  />
                </svg>
              </Link>
            ))}
          </nav>
        </div>

        {/* 인기 태그 */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">인기 태그</h3>
          <div className="flex flex-wrap gap-2">
            {popularTags.map((tag) => (
              <Link
                key={tag}
                href={`/posts?tag=${encodeURIComponent(tag)}`}
                className="px-3 py-1 bg-gray-100 dark:bg-gray-700 hover:bg-blue-100 dark:hover:bg-blue-900/30 text-gray-700 dark:text-gray-300 hover:text-blue-600 dark:hover:text-blue-400 rounded-full text-sm transition-colors"
              >
                #{tag}
              </Link>
            ))}
          </div>
        </div>

        {/* 인기 게시글 */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">인기 게시글</h3>
          {loading ? (
            <div className="space-y-3">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="animate-pulse">
                  <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-2"></div>
                  <div className="h-3 bg-gray-200 dark:bg-gray-700 rounded w-1/2"></div>
                </div>
              ))}
            </div>
          ) : popularPosts.length > 0 ? (
            <div className="space-y-4">
              {popularPosts.map((post, index) => (
                <Link
                  key={post.id}
                  href={`/posts/${post.id}`}
                  className="block group"
                >
                  <div className="flex items-start space-x-2">
                    <span className="flex-shrink-0 w-6 h-6 flex items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 text-xs font-semibold">
                      {index + 1}
                    </span>
                    <div className="flex-1 min-w-0">
                      <h4 className="text-sm font-medium text-gray-900 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400 line-clamp-2 transition-colors">
                        {post.title}
                      </h4>
                      <div className="flex items-center gap-2 mt-1 text-xs text-gray-500 dark:text-gray-400">
                        <span>👁️ {post.viewCount}</span>
                        <span>❤️ {post.likeCount}</span>
                      </div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <p className="text-sm text-gray-500 dark:text-gray-400">인기 게시글이 없습니다.</p>
          )}
        </div>

        {/* 트렌딩 게시글 */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4 flex items-center">
            <span className="mr-2">🔥</span>
            트렌딩
          </h3>
          {loading ? (
            <div className="space-y-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="animate-pulse">
                  <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-2"></div>
                  <div className="h-3 bg-gray-200 dark:bg-gray-700 rounded w-1/2"></div>
                </div>
              ))}
            </div>
          ) : trendingPosts.length > 0 ? (
            <div className="space-y-3">
              {trendingPosts.slice(0, 3).map((post) => (
                <Link
                  key={post.id}
                  href={`/posts/${post.id}`}
                  className="block group"
                >
                  <h4 className="text-sm font-medium text-gray-900 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400 line-clamp-2 transition-colors">
                    {post.title}
                  </h4>
                  <div className="flex items-center gap-2 mt-1 text-xs text-gray-500 dark:text-gray-400">
                    <span>👁️ {post.viewCount}</span>
                    <span>❤️ {post.likeCount}</span>
                    <span>💬 {post.commentCount}</span>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <p className="text-sm text-gray-500 dark:text-gray-400">트렌딩 게시글이 없습니다.</p>
          )}
        </div>
      </div>
    </aside>
  );
}
