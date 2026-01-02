'use client';

import { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/lib/hooks';
import { Button, Input, Card } from '@/components/ui';
import { validateEmail } from '@/lib/utils';

export default function LoginPage() {
  const router = useRouter();
  const { login, isLoading, error, clearError } = useAuth();

  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });

  const [formErrors, setFormErrors] = useState({
    email: '',
    password: '',
  });

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    clearError();

    // Validation
    const errors = { email: '', password: '' };
    let hasError = false;

    if (!formData.email) {
      errors.email = '이메일을 입력해주세요.';
      hasError = true;
    } else if (!validateEmail(formData.email)) {
      errors.email = '올바른 이메일 형식이 아닙니다.';
      hasError = true;
    }

    if (!formData.password) {
      errors.password = '비밀번호를 입력해주세요.';
      hasError = true;
    }

    if (hasError) {
      setFormErrors(errors);
      return;
    }

    try {
      await login(formData);
      router.push('/');
    } catch (err) {
      console.error('Login failed:', err);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">로그인</h1>
          <p className="mt-2 text-sm text-gray-600">
            Community Platform에 오신 것을 환영합니다
          </p>
        </div>

        <Card>
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                {error}
              </div>
            )}

            <Input
              label="이메일"
              type="email"
              value={formData.email}
              onChange={(e) => {
                setFormData({ ...formData, email: e.target.value });
                setFormErrors({ ...formErrors, email: '' });
              }}
              error={formErrors.email}
              placeholder="example@email.com"
              autoComplete="email"
              required
            />

            <Input
              label="비밀번호"
              type="password"
              value={formData.password}
              onChange={(e) => {
                setFormData({ ...formData, password: e.target.value });
                setFormErrors({ ...formErrors, password: '' });
              }}
              error={formErrors.password}
              placeholder="••••••••"
              autoComplete="current-password"
              required
            />

            <Button
              type="submit"
              className="w-full"
              isLoading={isLoading}
              disabled={isLoading}
            >
              {isLoading ? '로그인 중...' : '로그인'}
            </Button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              계정이 없으신가요?{' '}
              <Link href="/register" className="text-blue-600 hover:text-blue-500 font-medium">
                회원가입
              </Link>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
}
