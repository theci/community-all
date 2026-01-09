'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/hooks';
import { userService } from '@/lib/services';
import { Card, Button } from '@ddd3/design-system';

export default function ProfileEditPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading, user } = useAuth();

  const [formData, setFormData] = useState({
    profileImageUrl: '',
    bio: '',
    birthDate: '',
    gender: '' as '' | 'MALE' | 'FEMALE' | 'OTHER',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login?redirect=/profile/edit');
      return;
    }

    if (user?.profile) {
      setFormData({
        profileImageUrl: user.profile.profileImageUrl || '',
        bio: user.profile.bio || '',
        birthDate: user.profile.birthDate || '',
        gender: user.profile.gender || '',
      });
    }
  }, [authLoading, isAuthenticated, user, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const updateData: {
        profileImageUrl?: string;
        bio?: string;
        birthDate?: string;
        gender?: 'MALE' | 'FEMALE' | 'OTHER';
      } = {};

      if (formData.profileImageUrl) updateData.profileImageUrl = formData.profileImageUrl;
      if (formData.bio) updateData.bio = formData.bio;
      if (formData.birthDate) updateData.birthDate = formData.birthDate;
      if (formData.gender) updateData.gender = formData.gender;

      await userService.updateProfile(updateData);

      setSuccess(true);
      setTimeout(() => {
        router.push('/profile');
      }, 1500);
    } catch (err: any) {
      console.error('Failed to update profile:', err);
      setError(err.response?.data?.message || '프로필 업데이트에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    router.push('/profile');
  };

  if (authLoading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-500"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <div className="max-w-3xl mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-8">
          프로필 편집
        </h1>

        {error && (
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        {success && (
          <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 text-green-700 dark:text-green-400 px-4 py-3 rounded-lg mb-6">
            프로필이 성공적으로 업데이트되었습니다. 프로필 페이지로 이동합니다...
          </div>
        )}

        <Card>
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 프로필 이미지 URL */}
            <div>
              <label
                htmlFor="profileImageUrl"
                className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2"
              >
                프로필 이미지 URL
              </label>
              <input
                type="url"
                id="profileImageUrl"
                value={formData.profileImageUrl}
                onChange={(e) =>
                  setFormData({ ...formData, profileImageUrl: e.target.value })
                }
                className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-800 text-gray-900 dark:text-white"
                placeholder="https://example.com/profile.jpg"
              />
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                프로필 이미지 URL을 입력하세요 (최대 500자)
              </p>
            </div>

            {/* 자기소개 */}
            <div>
              <label
                htmlFor="bio"
                className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2"
              >
                자기소개
              </label>
              <textarea
                id="bio"
                value={formData.bio}
                onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                rows={4}
                maxLength={1000}
                className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-800 text-gray-900 dark:text-white resize-none"
                placeholder="자신을 소개해주세요"
              />
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                {formData.bio.length}/1000 자
              </p>
            </div>

            {/* 생년월일 */}
            <div>
              <label
                htmlFor="birthDate"
                className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2"
              >
                생년월일
              </label>
              <input
                type="date"
                id="birthDate"
                value={formData.birthDate}
                onChange={(e) =>
                  setFormData({ ...formData, birthDate: e.target.value })
                }
                max={new Date().toISOString().split('T')[0]}
                className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-800 text-gray-900 dark:text-white"
              />
            </div>

            {/* 성별 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                성별
              </label>
              <div className="flex gap-4">
                <label className="flex items-center cursor-pointer">
                  <input
                    type="radio"
                    name="gender"
                    value="MALE"
                    checked={formData.gender === 'MALE'}
                    onChange={(e) =>
                      setFormData({ ...formData, gender: e.target.value as 'MALE' })
                    }
                    className="w-4 h-4 text-blue-600 focus:ring-blue-500 border-gray-300 dark:border-gray-600"
                  />
                  <span className="ml-2 text-gray-700 dark:text-gray-300">남성</span>
                </label>
                <label className="flex items-center cursor-pointer">
                  <input
                    type="radio"
                    name="gender"
                    value="FEMALE"
                    checked={formData.gender === 'FEMALE'}
                    onChange={(e) =>
                      setFormData({ ...formData, gender: e.target.value as 'FEMALE' })
                    }
                    className="w-4 h-4 text-blue-600 focus:ring-blue-500 border-gray-300 dark:border-gray-600"
                  />
                  <span className="ml-2 text-gray-700 dark:text-gray-300">여성</span>
                </label>
                <label className="flex items-center cursor-pointer">
                  <input
                    type="radio"
                    name="gender"
                    value="OTHER"
                    checked={formData.gender === 'OTHER'}
                    onChange={(e) =>
                      setFormData({ ...formData, gender: e.target.value as 'OTHER' })
                    }
                    className="w-4 h-4 text-blue-600 focus:ring-blue-500 border-gray-300 dark:border-gray-600"
                  />
                  <span className="ml-2 text-gray-700 dark:text-gray-300">기타</span>
                </label>
                <label className="flex items-center cursor-pointer">
                  <input
                    type="radio"
                    name="gender"
                    value=""
                    checked={formData.gender === ''}
                    onChange={(e) =>
                      setFormData({ ...formData, gender: '' })
                    }
                    className="w-4 h-4 text-blue-600 focus:ring-blue-500 border-gray-300 dark:border-gray-600"
                  />
                  <span className="ml-2 text-gray-700 dark:text-gray-300">선택 안 함</span>
                </label>
              </div>
            </div>

            {/* 버튼 */}
            <div className="flex gap-3 pt-4">
              <Button
                type="submit"
                disabled={loading}
                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white disabled:bg-gray-400"
              >
                {loading ? '저장 중...' : '저장'}
              </Button>
              <Button
                type="button"
                onClick={handleCancel}
                disabled={loading}
                className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-700 dark:bg-gray-700 dark:hover:bg-gray-600 dark:text-gray-300"
              >
                취소
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}
