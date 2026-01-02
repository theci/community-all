'use client';

import { useState, useEffect, FormEvent } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { postService, categoryService } from '@/lib/services';
import { Category, Post, PostStatus } from '@/lib/types';
import { useAuth } from '@/lib/hooks';
import { Button, Input, Card } from '@/components/ui';

export default function EditPostPage() {
  const router = useRouter();
  const params = useParams();
  const postId = Number(params.id);
  const { user, isAuthenticated, isLoading: authLoading } = useAuth();

  const [post, setPost] = useState<Post | null>(null);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [summary, setSummary] = useState('');
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined);
  const [tags, setTags] = useState<string>('');

  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // 인증 확인
    if (!authLoading && !isAuthenticated) {
      alert('로그인이 필요합니다.');
      router.push(`/login?redirect=/posts/${postId}/edit`);
      return;
    }

    // 데이터 로드
    loadPost();
    loadCategories();
  }, [authLoading, isAuthenticated, postId, router]);

  const loadPost = async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await postService.getPost(postId);
      setPost(data);

      // 폼 데이터 설정
      setTitle(data.title);
      setContent(data.content);
      setSummary(data.summary || '');
      setCategoryId(data.category?.id);
      setTags(data.tags?.map(tag => tag.name).join(', ') || '');

      // 권한 확인
      if (user && data.author.id !== user.id) {
        alert('게시글을 수정할 권한이 없습니다.');
        router.push(`/posts/${postId}`);
      }
    } catch (err: any) {
      console.error('Failed to load post:', err);
      setError(err.response?.data?.message || '게시글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const loadCategories = async () => {
    try {
      const data = await categoryService.getCategories();
      setCategories(data || []);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    // 유효성 검사
    if (!title.trim()) {
      setError('제목을 입력해주세요.');
      return;
    }

    if (!content.trim()) {
      setError('내용을 입력해주세요.');
      return;
    }

    if (!user?.id) {
      setError('사용자 정보를 불러올 수 없습니다. 다시 로그인해주세요.');
      return;
    }

    try {
      setSaving(true);

      const tagArray = tags
        .split(',')
        .map(tag => tag.trim())
        .filter(tag => tag.length > 0);

      const updateData = {
        title: title.trim(),
        content: content.trim(),
        summary: summary.trim() || undefined,
        categoryId: categoryId,
        tagIds: undefined,
      };

      await postService.updatePost(postId, updateData, user.id);

      alert('게시글이 수정되었습니다.');
      router.push(`/posts/${postId}`);
    } catch (err: any) {
      console.error('Failed to update post:', err);
      setError(err.response?.data?.message || '게시글 수정에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  };

  if (authLoading || loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  if (error && !post) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">게시글을 불러올 수 없습니다</h2>
          <p className="text-gray-600 mb-6">{error}</p>
          <Link href="/posts">
            <Button className="bg-blue-600 hover:bg-blue-700 text-white">
              목록으로 돌아가기
            </Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-6">
          <Link href={`/posts/${postId}`} className="text-blue-600 hover:text-blue-700 font-medium">
            ← 뒤로 가기
          </Link>
        </div>

        <h1 className="text-3xl font-bold text-gray-900 mb-8">게시글 수정</h1>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        <Card>
          <form onSubmit={handleSubmit}>
            {/* 카테고리 선택 */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                카테고리
              </label>
              <select
                value={categoryId || ''}
                onChange={(e) => setCategoryId(e.target.value ? Number(e.target.value) : undefined)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900"
              >
                <option value="">카테고리 선택 (선택사항)</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>

            {/* 제목 */}
            <div className="mb-6">
              <Input
                label="제목"
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="제목을 입력하세요"
                required
                maxLength={200}
              />
              <p className="mt-1 text-sm text-gray-500 text-right">
                {title.length}/200
              </p>
            </div>

            {/* 요약 */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                요약 (선택사항)
              </label>
              <textarea
                value={summary}
                onChange={(e) => setSummary(e.target.value)}
                placeholder="게시글 요약을 입력하세요"
                rows={2}
                maxLength={300}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900 placeholder:text-gray-400"
              />
              <p className="mt-1 text-sm text-gray-500 text-right">
                {summary.length}/300
              </p>
            </div>

            {/* 본문 */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                본문 <span className="text-red-500">*</span>
              </label>
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="내용을 입력하세요"
                rows={15}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900 placeholder:text-gray-400"
              />
            </div>

            {/* 태그 */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                태그 (선택사항)
              </label>
              <Input
                type="text"
                value={tags}
                onChange={(e) => setTags(e.target.value)}
                placeholder="태그를 쉼표로 구분하여 입력하세요 (예: React, TypeScript, Next.js)"
              />
              <p className="mt-1 text-sm text-gray-500">
                태그는 쉼표(,)로 구분하여 입력하세요.
              </p>
            </div>

            {/* 버튼 */}
            <div className="flex justify-end gap-3 pt-6 border-t border-gray-200">
              <Link href={`/posts/${postId}`}>
                <Button
                  type="button"
                  className="bg-gray-200 hover:bg-gray-300 text-gray-700"
                  disabled={saving}
                >
                  취소
                </Button>
              </Link>
              <Button
                type="submit"
                className="bg-blue-600 hover:bg-blue-700 text-white"
                disabled={saving}
                isLoading={saving}
              >
                {saving ? '저장 중...' : '수정 완료'}
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}
