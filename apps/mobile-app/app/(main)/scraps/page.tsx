'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/lib/hooks';
import { scrapService } from '@/lib/services';
import { Card, Button, Input, Modal } from '@/components/ui';
import type { ScrapFolder, PostScrap } from '@/lib/types';

export default function ScrapsPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();

  const [folders, setFolders] = useState<ScrapFolder[]>([]);
  const [selectedFolder, setSelectedFolder] = useState<ScrapFolder | null>(null);
  const [scraps, setScraps] = useState<PostScrap[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 폴더 생성/수정 모달
  const [showFolderModal, setShowFolderModal] = useState(false);
  const [editingFolder, setEditingFolder] = useState<ScrapFolder | null>(null);
  const [folderName, setFolderName] = useState('');
  const [folderDescription, setFolderDescription] = useState('');

  // 페이지 마운트 시 스크롤을 맨 위로
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login?redirect=/scraps');
      return;
    }

    if (user) {
      loadFolders();
    }
  }, [authLoading, isAuthenticated, user, router]);

  const loadFolders = async () => {
    if (!user) return;

    try {
      setLoading(true);
      setError(null);

      const foldersData = await scrapService.getMyFolders(user.id);
      setFolders(foldersData);

      // 기본 폴더 선택
      if (foldersData.length > 0) {
        const defaultFolder = foldersData.find((f) => f.isDefault) || foldersData[0];
        selectFolder(defaultFolder);
      }
    } catch (err: any) {
      console.error('Failed to load folders:', err);
      setError(err.response?.data?.message || '스크랩 폴더를 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const selectFolder = async (folder: ScrapFolder) => {
    if (!user) return;

    setSelectedFolder(folder);
    try {
      const scrapsData = await scrapService.getScrapsInFolder(folder.id, user.id);
      setScraps(scrapsData);
    } catch (err) {
      console.error('Failed to load scraps:', err);
      setScraps([]);
    }
  };

  const handleCreateFolder = () => {
    setEditingFolder(null);
    setFolderName('');
    setFolderDescription('');
    setShowFolderModal(true);
  };

  const handleEditFolder = (folder: ScrapFolder) => {
    setEditingFolder(folder);
    setFolderName(folder.name);
    setFolderDescription(folder.description || '');
    setShowFolderModal(true);
  };

  const handleSaveFolder = async () => {
    if (!user) return;

    if (!folderName.trim()) {
      alert('폴더 이름을 입력해주세요.');
      return;
    }

    try {
      if (editingFolder) {
        // 폴더 수정
        await scrapService.updateFolder(
          editingFolder.id,
          {
            name: folderName.trim(),
            description: folderDescription.trim() || undefined,
          },
          user.id
        );
      } else {
        // 폴더 생성
        await scrapService.createFolder(
          {
            name: folderName.trim(),
            description: folderDescription.trim() || undefined,
          },
          user.id
        );
      }

      setShowFolderModal(false);
      loadFolders();
    } catch (err: any) {
      console.error('Failed to save folder:', err);
      alert(err.response?.data?.message || '폴더 저장에 실패했습니다.');
    }
  };

  const handleDeleteFolder = async (folder: ScrapFolder) => {
    if (!user) return;

    if (folder.isDefault) {
      alert('기본 폴더는 삭제할 수 없습니다.');
      return;
    }

    if (!confirm(`"${folder.name}" 폴더를 삭제하시겠습니까?\n폴더 내 모든 스크랩이 삭제됩니다.`)) {
      return;
    }

    try {
      await scrapService.deleteFolder(folder.id, user.id);
      loadFolders();
    } catch (err: any) {
      console.error('Failed to delete folder:', err);
      alert(err.response?.data?.message || '폴더 삭제에 실패했습니다.');
    }
  };

  const handleRemoveScrap = async (scrap: PostScrap) => {
    if (!confirm('이 게시글을 스크랩에서 제거하시겠습니까?')) {
      return;
    }

    try {
      // TODO: API 구현 필요
      // await scrapService.removeScrap(scrap.id);
      alert('스크랩 제거 기능은 곧 제공될 예정입니다.');
    } catch (err: any) {
      console.error('Failed to remove scrap:', err);
      alert(err.response?.data?.message || '스크랩 제거에 실패했습니다.');
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

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* 헤더 */}
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">스크랩 폴더</h1>
            <p className="text-gray-600">
              관심 있는 게시글을 폴더별로 정리하세요
            </p>
          </div>
          <Button onClick={handleCreateFolder} className="bg-blue-600 hover:bg-blue-700">
            + 새 폴더
          </Button>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* 폴더 사이드바 */}
          <div className="lg:col-span-1">
            <Card className="p-0">
              <div className="p-4 border-b border-gray-200">
                <h2 className="font-semibold text-gray-900">내 폴더</h2>
              </div>
              <div className="divide-y divide-gray-200">
                {folders.length === 0 ? (
                  <div className="p-4 text-center text-gray-500 text-sm">
                    폴더가 없습니다
                  </div>
                ) : (
                  folders.map((folder) => (
                    <div
                      key={folder.id}
                      className={`p-4 cursor-pointer transition-colors ${
                        selectedFolder?.id === folder.id
                          ? 'bg-blue-50 border-l-4 border-blue-600'
                          : 'hover:bg-gray-50'
                      }`}
                      onClick={() => selectFolder(folder)}
                    >
                      <div className="flex items-center justify-between mb-1">
                        <div className="flex items-center gap-2">
                          <span className="text-lg">📁</span>
                          <span className="font-medium text-gray-900">
                            {folder.name}
                          </span>
                          {folder.isDefault && (
                            <span className="text-xs bg-blue-100 text-blue-600 px-2 py-0.5 rounded">
                              기본
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-gray-600">
                          {folder.scrapCount}개
                        </span>
                        {!folder.isDefault && (
                          <div className="flex gap-1">
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                handleEditFolder(folder);
                              }}
                              className="text-xs text-gray-600 hover:text-blue-600"
                            >
                              수정
                            </button>
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                handleDeleteFolder(folder);
                              }}
                              className="text-xs text-gray-600 hover:text-red-600"
                            >
                              삭제
                            </button>
                          </div>
                        )}
                      </div>
                      {folder.description && (
                        <p className="text-xs text-gray-500 mt-1 line-clamp-2">
                          {folder.description}
                        </p>
                      )}
                    </div>
                  ))
                )}
              </div>
            </Card>
          </div>

          {/* 스크랩 목록 */}
          <div className="lg:col-span-3">
            {selectedFolder ? (
              <div>
                <div className="mb-4 flex items-center justify-between">
                  <h2 className="text-xl font-semibold text-gray-900">
                    {selectedFolder.name}
                  </h2>
                  <span className="text-gray-600">
                    {scraps.length}개의 스크랩
                  </span>
                </div>

                {scraps.length === 0 ? (
                  <Card>
                    <div className="text-center py-12 text-gray-500">
                      <p className="text-lg mb-2">스크랩한 게시글이 없습니다.</p>
                      <p className="text-sm">
                        게시글 상세 페이지에서 스크랩할 수 있습니다.
                      </p>
                    </div>
                  </Card>
                ) : (
                  <div className="space-y-4">
                    {scraps.map((scrap) => (
                      <Card key={scrap.id} className="hover:shadow-md transition-shadow">
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            {scrap.postId ? (
                              <Link href={`/posts/${scrap.postId}`}>
                                <h3 className="text-lg font-semibold text-gray-900 mb-2 hover:text-blue-600">
                                  {scrap.post?.title || '제목 없음'}
                                </h3>
                              </Link>
                            ) : (
                              <div>
                                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                                  {scrap.post?.title || '제목 없음'}
                                </h3>
                                <p className="text-sm text-red-600 mb-2">
                                  ⚠️ 백엔드 버그: 게시글 정보가 누락되었습니다
                                </p>
                              </div>
                            )}
                            {scrap.post?.summary && (
                              <p className="text-gray-600 mb-3 line-clamp-2">
                                {scrap.post.summary}
                              </p>
                            )}
                            <div className="flex items-center gap-4 text-sm text-gray-500">
                              {scrap.post ? (
                                <>
                                  <span>👁️ {scrap.post.viewCount}</span>
                                  <span>❤️ {scrap.post.likeCount}</span>
                                  <span>💬 {scrap.post.commentCount}</span>
                                  <span>
                                    {new Date(scrap.createdAt).toLocaleDateString()}
                                  </span>
                                </>
                              ) : (
                                <span>
                                  스크랩 날짜: {new Date(scrap.createdAt).toLocaleDateString()}
                                </span>
                              )}
                            </div>
                          </div>
                          <button
                            onClick={() => handleRemoveScrap(scrap)}
                            className="ml-4 text-gray-400 hover:text-red-600 transition-colors"
                            title="스크랩 제거"
                          >
                            🗑️
                          </button>
                        </div>
                      </Card>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <Card>
                <div className="text-center py-12 text-gray-500">
                  <p>폴더를 선택해주세요</p>
                </div>
              </Card>
            )}
          </div>
        </div>

        {/* 폴더 생성/수정 모달 */}
        {showFolderModal && (
          <Modal
            isOpen={showFolderModal}
            onClose={() => setShowFolderModal(false)}
            title={editingFolder ? '폴더 수정' : '새 폴더 만들기'}
          >
            <div className="space-y-4">
              <Input
                label="폴더 이름"
                value={folderName}
                onChange={(e) => setFolderName(e.target.value)}
                placeholder="폴더 이름을 입력하세요"
                required
                maxLength={50}
              />
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  설명 (선택사항)
                </label>
                <textarea
                  value={folderDescription}
                  onChange={(e) => setFolderDescription(e.target.value)}
                  placeholder="폴더 설명을 입력하세요"
                  rows={3}
                  maxLength={200}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900"
                />
              </div>
              <div className="flex justify-end gap-3 pt-4">
                <Button
                  onClick={() => setShowFolderModal(false)}
                  className="bg-gray-200 hover:bg-gray-300 text-gray-700"
                >
                  취소
                </Button>
                <Button
                  onClick={handleSaveFolder}
                  className="bg-blue-600 hover:bg-blue-700"
                >
                  {editingFolder ? '수정' : '생성'}
                </Button>
              </div>
            </div>
          </Modal>
        )}
      </div>
    </div>
  );
}
