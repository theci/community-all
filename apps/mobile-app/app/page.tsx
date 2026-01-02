'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useAuth } from '@/lib/hooks';
import { postService } from '@/lib/services';
import Header from '@/components/layout/Header';
import Footer from '@/components/layout/Footer';
import Sidebar from '@/components/layout/Sidebar';
import type { Post } from '@/lib/types';

export default function Home() {
  const { isAuthenticated, user } = useAuth();
  const [mounted, setMounted] = useState(false);
  const [popularPosts, setPopularPosts] = useState<Post[]>([]);
  const [trendingPosts, setTrendingPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setMounted(true);
    loadPosts();
  }, []);

  const loadPosts = async () => {
    try {
      setLoading(true);
      const [popular, trending] = await Promise.all([
        postService.getPopularPosts(6),
        postService.getTrendingPosts(6),
      ]);

      // 배열인지 확인
      setPopularPosts(Array.isArray(popular) ? popular : []);
      setTrendingPosts(Array.isArray(trending) ? trending : []);
    } catch (err) {
      console.error('Failed to load posts:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-50 dark:bg-gray-900">
      <Header />
      <main className="flex-1">
      {/* 히어로 섹션 */}
      <section className="bg-gradient-to-r from-blue-600 to-blue-800 text-white py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h1 className="text-5xl font-bold mb-6">
              Community Platform
            </h1>
            <p className="text-xl mb-8 text-blue-100">
              지식을 공유하고 소통하는 커뮤니티
            </p>
            <div className="flex gap-4 justify-center">
              {!mounted ? (
                <div className="animate-pulse h-12 w-32 bg-blue-500 rounded-lg"></div>
              ) : isAuthenticated ? (
                <Link
                  href="/posts/create"
                  className="px-8 py-3 bg-white text-blue-600 rounded-lg hover:bg-blue-50 transition-colors font-medium text-lg"
                >
                  글쓰기
                </Link>
              ) : (
                <>
                  <Link
                    href="/register"
                    className="px-8 py-3 bg-white text-blue-600 rounded-lg hover:bg-blue-50 transition-colors font-medium text-lg"
                  >
                    시작하기
                  </Link>
                  <Link
                    href="/posts"
                    className="px-8 py-3 bg-blue-700 text-white rounded-lg hover:bg-blue-800 transition-colors font-medium text-lg border-2 border-blue-500"
                  >
                    둘러보기
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* 메인 콘텐츠 */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="flex gap-8">
          {/* 왼쪽 콘텐츠 */}
          <div className="flex-1">
            {/* 인기 게시글 */}
            <section className="mb-12">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-gray-900 dark:text-white">인기 게시글</h2>
                <Link
                  href="/posts?sort=popular"
                  className="text-blue-600 hover:text-blue-700 font-medium"
                >
                  더보기 →
                </Link>
              </div>

              {loading ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 animate-pulse">
                      <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-3"></div>
                      <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-full mb-2"></div>
                      <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-2/3"></div>
                    </div>
                  ))}
                </div>
              ) : popularPosts.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {popularPosts.map((post) => (
                    <Link
                      key={post.id}
                      href={`/posts/${post.id}`}
                      className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 hover:shadow-md transition-shadow"
                    >
                      <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2 line-clamp-2">
                        {post.title}
                      </h3>
                      {post.summary && (
                        <p className="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-2">
                          {post.summary}
                        </p>
                      )}
                      <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
                        <span>👁️ {post.viewCount}</span>
                        <span>❤️ {post.likeCount}</span>
                        <span>💬 {post.commentCount}</span>
                      </div>
                    </Link>
                  ))}
                </div>
              ) : (
                <div className="bg-white dark:bg-gray-800 p-12 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 text-center text-gray-500 dark:text-gray-400">
                  게시글이 없습니다.
                </div>
              )}
            </section>

            {/* 트렌딩 게시글 */}
            <section>
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
                  <span className="mr-2">🔥</span>
                  트렌딩
                </h2>
                <Link
                  href="/posts?sort=trending"
                  className="text-blue-600 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 font-medium"
                >
                  더보기 →
                </Link>
              </div>

              {loading ? (
                <div className="grid grid-cols-1 gap-4">
                  {[1, 2, 3].map((i) => (
                    <div key={i} className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 animate-pulse">
                      <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-3"></div>
                      <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/2"></div>
                    </div>
                  ))}
                </div>
              ) : trendingPosts.length > 0 ? (
                <div className="grid grid-cols-1 gap-4">
                  {trendingPosts.map((post) => (
                    <Link
                      key={post.id}
                      href={`/posts/${post.id}`}
                      className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 hover:shadow-md transition-shadow"
                    >
                      <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2 line-clamp-1">
                        {post.title}
                      </h3>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
                          <span>👁️ {post.viewCount}</span>
                          <span>❤️ {post.likeCount}</span>
                          <span>💬 {post.commentCount}</span>
                        </div>
                        <span className="text-sm text-gray-500 dark:text-gray-400">
                          {new Date(post.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </Link>
                  ))}
                </div>
              ) : (
                <div className="bg-white dark:bg-gray-800 p-12 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 text-center text-gray-500 dark:text-gray-400">
                  트렌딩 게시글이 없습니다.
                </div>
              )}
            </section>
          </div>

          {/* 사이드바 */}
          <Sidebar />
        </div>
      </div>
      </main>
      <Footer />
    </div>
  );
}
