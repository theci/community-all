'use client';

import { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/lib/hooks';
import { Button, Input, Card } from '@/components/ui';
import { validateEmail, validatePassword, validateUsername } from '@/lib/utils';

export default function RegisterPage() {
  const router = useRouter();
  const { register, isLoading, error, clearError } = useAuth();

  const [formData, setFormData] = useState({
    email: '',
    password: '',
    passwordConfirm: '',
    username: '',
    nickname: '',
  });

  const [formErrors, setFormErrors] = useState({
    email: '',
    password: '',
    passwordConfirm: '',
    username: '',
    nickname: '',
  });

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    clearError();

    // Validation
    const errors = {
      email: '',
      password: '',
      passwordConfirm: '',
      username: '',
      nickname: '',
    };
    let hasError = false;

    if (!formData.email) {
      errors.email = '이메일을 입력해주세요.';
      hasError = true;
    } else if (!validateEmail(formData.email)) {
      errors.email = '올바른 이메일 형식이 아닙니다.';
      hasError = true;
    }

    if (!formData.username) {
      errors.username = '사용자명을 입력해주세요.';
      hasError = true;
    } else if (!validateUsername(formData.username)) {
      errors.username = '사용자명은 3-20자의 영문, 숫자, 언더스코어만 사용 가능합니다.';
      hasError = true;
    }

    if (!formData.nickname) {
      errors.nickname = '닉네임을 입력해주세요.';
      hasError = true;
    }

    if (!formData.password) {
      errors.password = '비밀번호를 입력해주세요.';
      hasError = true;
    } else {
      const passwordValidation = validatePassword(formData.password);
      if (!passwordValidation.isValid) {
        errors.password = passwordValidation.errors.join(' ');
        hasError = true;
      }
    }

    if (!formData.passwordConfirm) {
      errors.passwordConfirm = '비밀번호 확인을 입력해주세요.';
      hasError = true;
    } else if (formData.password !== formData.passwordConfirm) {
      errors.passwordConfirm = '비밀번호가 일치하지 않습니다.';
      hasError = true;
    }

    if (hasError) {
      setFormErrors(errors);
      return;
    }

    try {
      await register({
        email: formData.email,
        password: formData.password,
        username: formData.username,
        nickname: formData.nickname,
      });

      alert('회원가입이 완료되었습니다. 로그인해주세요.');
      router.push('/login');
    } catch (err) {
      console.error('Registration failed:', err);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">회원가입</h1>
          <p className="mt-2 text-sm text-gray-600">
            Community Platform 회원이 되어보세요
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
              required
            />

            <Input
              label="사용자명"
              type="text"
              value={formData.username}
              onChange={(e) => {
                setFormData({ ...formData, username: e.target.value });
                setFormErrors({ ...formErrors, username: '' });
              }}
              error={formErrors.username}
              placeholder="username123"
              helperText="3-20자의 영문, 숫자, 언더스코어만 사용 가능"
              required
            />

            <Input
              label="닉네임"
              type="text"
              value={formData.nickname}
              onChange={(e) => {
                setFormData({ ...formData, nickname: e.target.value });
                setFormErrors({ ...formErrors, nickname: '' });
              }}
              error={formErrors.nickname}
              placeholder="홍길동"
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
              helperText="최소 8자, 대소문자, 숫자 포함"
              required
            />

            <Input
              label="비밀번호 확인"
              type="password"
              value={formData.passwordConfirm}
              onChange={(e) => {
                setFormData({ ...formData, passwordConfirm: e.target.value });
                setFormErrors({ ...formErrors, passwordConfirm: '' });
              }}
              error={formErrors.passwordConfirm}
              placeholder="••••••••"
              required
            />

            <Button
              type="submit"
              className="w-full"
              isLoading={isLoading}
              disabled={isLoading}
            >
              {isLoading ? '가입 중...' : '회원가입'}
            </Button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              이미 계정이 있으신가요?{' '}
              <Link href="/login" className="text-blue-600 hover:text-blue-500 font-medium">
                로그인
              </Link>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
}
