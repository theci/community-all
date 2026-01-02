'use client';

import { useState, useEffect, FormEvent, ChangeEvent } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { postService, categoryService, attachmentService } from '@/lib/services';
import { Category, PostStatus } from '@/lib/types';
import { useAuth } from '@/lib/hooks';
import { Button, Input, Card } from '@/components/ui';

export default function CreatePostPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [summary, setSummary] = useState('');
  const [categoryId, setCategoryId] = useState<number>(1);  // 기본값 1번 카테고리
  const [tags, setTags] = useState<string>('');
  const [showPreview, setShowPreview] = useState(false);
  const [imageFiles, setImageFiles] = useState<{ file: File; localUrl: string }[]>([]);

  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // 디버깅: 사용자 정보 확인
    console.log('Auth State:', { authLoading, isAuthenticated, user });

    // 인증 확인
    if (!authLoading && !isAuthenticated) {
      alert('로그인이 필요합니다.');
      router.push('/login?redirect=/posts/create');
      return;
    }

    // 카테고리 로드
    loadCategories();
  }, [authLoading, isAuthenticated, user, router]);

  const loadCategories = async () => {
    try {
      const data = await categoryService.getCategories();
      setCategories(data || []);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const insertMarkdown = (before: string, after: string = '') => {
    const textarea = document.getElementById('content-textarea') as HTMLTextAreaElement;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selectedText = content.substring(start, end);
    const newText = content.substring(0, start) + before + selectedText + after + content.substring(end);

    setContent(newText);

    // 커서 위치 조정
    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(start + before.length, start + before.length + selectedText.length);
    }, 0);
  };

  const handleImageInsert = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.multiple = true;
    input.onchange = (e: Event) => {
      const target = e.target as HTMLInputElement;
      const files = Array.from(target.files || []);

      if (files.length === 0) return;

      // 파일 크기 검증
      const validFiles = files.filter((file) => {
        if (file.size > 10 * 1024 * 1024) {
          alert(`${file.name}: 파일 크기가 너무 큽니다. (최대 10MB)`);
          return false;
        }
        return true;
      });

      if (validFiles.length === 0) return;

      // 커서 위치 저장
      const textarea = document.getElementById('content-textarea') as HTMLTextAreaElement;
      const cursorPosition = textarea ? textarea.selectionStart : content.length;

      // 새로운 이미지 파일들을 state에 추가
      const newImageItems = validFiles.map((file) => ({
        file,
        localUrl: URL.createObjectURL(file),
      }));

      // 현재 imageFiles 길이를 기준으로 인덱스 계산
      const startIndex = imageFiles.length;

      // 마크다운 텍스트 생성
      const imageMarkdowns = newImageItems
        .map((item, i) => `\n![${item.file.name}](temp:${startIndex + i})\n`)
        .join('');

      // 상태 업데이트를 한 번에 처리
      setImageFiles((prev) => [...prev, ...newImageItems]);
      setContent((prev) =>
        prev.substring(0, cursorPosition) + imageMarkdowns + prev.substring(cursorPosition)
      );

      // 커서 위치 조정
      if (textarea) {
        setTimeout(() => {
          textarea.focus();
          textarea.setSelectionRange(
            cursorPosition + imageMarkdowns.length,
            cursorPosition + imageMarkdowns.length
          );
        }, 0);
      }
    };
    input.click();
  };

  const handleSubmit = async (e: FormEvent, status: PostStatus) => {
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

    // 디버깅: 사용자 정보 확인
    console.log('User info when submitting:', user);

    if (!user?.id) {
      console.error('User ID missing. Full user object:', user);
      setError('사용자 정보를 불러올 수 없습니다. 다시 로그인해주세요.');
      return;
    }

    try {
      setLoading(true);

      const tagArray = tags
        .split(',')
        .map(tag => tag.trim())
        .filter(tag => tag.length > 0);

      // 백엔드 API 요구사항에 맞춰 데이터 구성
      const postData: any = {
        title: title.trim(),
        content: content.trim(),
        contentType: 'MARKDOWN',  // 필수 필드!
        categoryId: categoryId,  // 필수 필드!
      };

      if (summary.trim()) {
        postData.summary = summary.trim();
      }

      // 태그 배열 추가 (있는 경우만)
      if (tagArray.length > 0) {
        postData.tags = tagArray;
      }

      console.log('=== POST 요청 정보 ===');
      console.log('User ID:', user.id);
      console.log('Post Data:', JSON.stringify(postData, null, 2));
      console.log('====================');

      const createdPost = await postService.createPost(postData, user.id);
      console.log('Post created:', createdPost);

      // 마크다운에 삽입된 이미지 파일 업로드 및 URL 교체
      let finalContent = content;
      if (imageFiles.length > 0) {
        try {
          console.log('=== 이미지 업로드 시작 ===');
          console.log('업로드할 이미지 개수:', imageFiles.length);
          console.log('원본 content:', content);

          const files = imageFiles.map((img) => img.file);
          const uploadedAttachments = await attachmentService.uploadAttachments(createdPost.id, files, user.id);

          console.log('업로드 완료된 첨부파일:', uploadedAttachments);

          // temp: URL을 S3 직접 URL로 교체
          uploadedAttachments.forEach((attachment, index) => {
            // S3 직접 URL 생성 (공개 버킷으로 설정 완료)
            const s3Url = `https://test-airgateway.s3.ap-northeast-2.amazonaws.com/${attachment.filePath}`;

            console.log(`\n=== 이미지 ${index + 1} URL 교체 ===`);
            console.log(`파일명: ${attachment.originalName}`);
            console.log(`filePath: ${attachment.filePath}`);
            console.log(`교체 대상: temp:${index}`);
            console.log(`S3 URL: ${s3Url}`);

            // 정규식을 사용해서 확실하게 교체
            const regex = new RegExp(`temp:${index}`, 'g');
            const beforeReplace = finalContent;
            finalContent = finalContent.replace(regex, s3Url);

            console.log(`교체 전 일치 개수:`, (beforeReplace.match(regex) || []).length);
            console.log(`교체 성공 여부:`, beforeReplace !== finalContent);
            console.log(`=====================\n`);
          });

          // 이미지가 있으면 무조건 게시글 업데이트 (URL 교체 반영)
          console.log('=== 게시글 업데이트 준비 ===');
          console.log('업로드된 이미지:', uploadedAttachments.length);
          console.log('Content 변경됨:', finalContent !== content);
          console.log('최종 content:', finalContent);

          // 기존 게시글의 모든 필드를 포함해서 업데이트
          const updateData: any = {
            title: createdPost.title,
            content: finalContent,
            contentType: 'MARKDOWN',
            categoryId: createdPost.category?.id,
          };

          if (createdPost.summary) {
            updateData.summary = createdPost.summary;
          }

          if (createdPost.tags && createdPost.tags.length > 0) {
            updateData.tags = createdPost.tags.map(tag => tag.name);
          }

          console.log('=== UPDATE 요청 정보 ===');
          console.log('Post ID:', createdPost.id);
          console.log('Update Data:', JSON.stringify(updateData, null, 2));
          console.log('======================');

          await postService.updatePost(createdPost.id, updateData, user.id);
          console.log('✅ 게시글 업데이트 완료');
        } catch (uploadErr) {
          console.error('Failed to upload images:', uploadErr);
          alert('이미지 업로드 중 일부 실패했습니다. 게시글은 저장되었습니다.');
        }
      }

      // 별도 파일 첨부는 제거 (모든 이미지는 본문에 직접 삽입)
      // selectedFiles는 더 이상 사용하지 않음

      // 발행 상태일 경우, publish API 호출
      if (status === PostStatus.PUBLISHED) {
        await postService.publishPost(createdPost.id, user.id);
        alert('게시글이 발행되었습니다.');
        router.push(`/posts/${createdPost.id}`);
      } else {
        alert('임시저장되었습니다.');
        router.push('/posts');
      }
    } catch (err: any) {
      console.error('Failed to create post:', err);
      console.error('Error response:', err.response?.data);

      const errorMessage = err.response?.data?.message
        || err.response?.data?.error
        || err.message
        || '게시글 작성에 실패했습니다.';

      setError(`게시글 작성 실패: ${errorMessage}`);
      alert(`게시글 작성 실패: ${errorMessage}`);
    } finally {
      setLoading(false);
    }
  };

  if (authLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-6">
          <Link href="/posts" className="text-blue-600 hover:text-blue-700 font-medium">
            ← 목록으로
          </Link>
        </div>

        <h1 className="text-3xl font-bold text-gray-900 mb-8">게시글 작성</h1>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        <Card>
          <form>
            {/* 카테고리 선택 */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                카테고리 <span className="text-red-500">*</span>
              </label>
              <select
                value={categoryId}
                onChange={(e) => setCategoryId(Number(e.target.value))}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900"
              >
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
              <div className="flex items-center justify-between mb-2">
                <label className="block text-sm font-medium text-gray-700">
                  본문 <span className="text-red-500">*</span>
                </label>
                <button
                  type="button"
                  onClick={() => setShowPreview(!showPreview)}
                  className="text-sm text-blue-600 hover:text-blue-700"
                >
                  {showPreview ? '편집' : '미리보기'}
                </button>
              </div>

              {/* 마크다운 툴바 */}
              {!showPreview && (
                <div className="flex flex-wrap gap-1 mb-2 p-2 bg-gray-50 border border-gray-300 rounded-t-lg">
                  <button
                    type="button"
                    onClick={() => insertMarkdown('**', '**')}
                    className="px-2 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-100"
                    title="굵게"
                  >
                    <strong>B</strong>
                  </button>
                  <button
                    type="button"
                    onClick={() => insertMarkdown('*', '*')}
                    className="px-2 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-100"
                    title="기울임"
                  >
                    <em>I</em>
                  </button>
                  <button
                    type="button"
                    onClick={() => insertMarkdown('# ', '')}
                    className="px-2 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-100"
                    title="제목"
                  >
                    H1
                  </button>
                  <button
                    type="button"
                    onClick={() => insertMarkdown('## ', '')}
                    className="px-2 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-100"
                    title="제목 2"
                  >
                    H2
                  </button>
                  <button
                    type="button"
                    onClick={() => insertMarkdown('- ', '')}
                    className="px-2 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-100"
                    title="목록"
                  >
                    •
                  </button>
                  <button
                    type="button"
                    onClick={handleImageInsert}
                    className="px-2 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-100 font-semibold"
                    title="이미지 삽입"
                  >
                    🖼️ 이미지
                  </button>
                  <button
                    type="button"
                    onClick={() => insertMarkdown('[', '](url)')}
                    className="px-2 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-100"
                    title="링크"
                  >
                    🔗
                  </button>
                  <button
                    type="button"
                    onClick={() => insertMarkdown('```\n', '\n```')}
                    className="px-2 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-100"
                    title="코드 블록"
                  >
                    {'</>'}
                  </button>
                  <button
                    type="button"
                    onClick={() => insertMarkdown('`', '`')}
                    className="px-2 py-1 text-sm bg-white border border-gray-300 rounded hover:bg-gray-100"
                    title="인라인 코드"
                  >
                    `code`
                  </button>
                </div>
              )}

              {!showPreview ? (
                <textarea
                  id="content-textarea"
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="마크다운 문법을 사용하여 내용을 작성하세요"
                  rows={15}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-b-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900 placeholder:text-gray-400 font-mono text-sm"
                />
              ) : (
                <div className="w-full px-4 py-3 border border-gray-300 rounded-lg bg-white min-h-[400px]">
                  {content ? (
                    <div className="prose max-w-none">
                      <ReactMarkdown
                        remarkPlugins={[remarkGfm]}
                        components={{
                          // 커스텀 이미지 렌더러: 유효한 URL만 렌더링
                          img: ({ node, src, alt, ...props }) => {
                            // src가 없거나 빈 문자열이면 렌더링하지 않음
                            if (!src || (typeof src === 'string' && src.trim() === '')) {
                              return <span className="text-red-500">[이미지 URL 없음: {alt}]</span>;
                            }

                            // temp: URL이면 로컬 blob URL로 교체해서 미리보기
                            if (typeof src === 'string' && src.startsWith('temp:')) {
                              const tempIndex = parseInt(src.replace('temp:', ''));
                              const imageFile = imageFiles[tempIndex];

                              if (imageFile?.localUrl) {
                                return (
                                  <img
                                    src={imageFile.localUrl}
                                    alt={alt || ''}
                                    {...props}
                                    className="max-w-full h-auto"
                                  />
                                );
                              } else {
                                return <span className="text-yellow-600">[이미지 로딩 중...]</span>;
                              }
                            }

                            // 일반 URL은 그대로 렌더링
                            return <img src={typeof src === 'string' ? src : ''} alt={alt || ''} {...props} className="max-w-full h-auto" />;
                          },
                        }}
                      >
                        {content}
                      </ReactMarkdown>
                    </div>
                  ) : (
                    <p className="text-gray-400">미리보기할 내용이 없습니다.</p>
                  )}
                </div>
              )}
              <p className="mt-1 text-sm text-gray-500">
                마크다운 문법을 사용할 수 있습니다. 이미지를 삽입하려면 🖼️ 이미지 버튼을 클릭하세요.
              </p>
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

            {/* 파일 첨부 섹션 제거 - 모든 이미지는 본문에 직접 삽입 */}
            {/* 아래 내용은 사용하지 않음 */}
            {false && (
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                추가 파일 첨부 (사용 안 함)
              </label>
            </div>
            )}

            {/* 버튼 */}
            <div className="flex justify-end gap-3 pt-6 border-t border-gray-200">
              <Link href="/posts">
                <Button
                  type="button"
                  className="bg-gray-200 hover:bg-gray-300 text-gray-700"
                  disabled={loading}
                >
                  취소
                </Button>
              </Link>
              <Button
                type="button"
                onClick={(e) => handleSubmit(e, PostStatus.DRAFT)}
                className="bg-gray-600 hover:bg-gray-700 text-white"
                disabled={loading}
              >
                임시저장
              </Button>
              <Button
                type="button"
                onClick={(e) => handleSubmit(e, PostStatus.PUBLISHED)}
                className="bg-blue-600 hover:bg-blue-700 text-white"
                disabled={loading}
                isLoading={loading}
              >
                {loading ? '발행 중...' : '발행'}
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}
