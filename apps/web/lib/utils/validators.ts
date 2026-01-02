export function validateEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

export function validatePassword(password: string): {
  isValid: boolean;
  errors: string[];
} {
  const errors: string[] = [];

  if (password.length < 8) {
    errors.push('비밀번호는 최소 8자 이상이어야 합니다.');
  }
  if (!/[A-Z]/.test(password)) {
    errors.push('대문자를 최소 1개 포함해야 합니다.');
  }
  if (!/[a-z]/.test(password)) {
    errors.push('소문자를 최소 1개 포함해야 합니다.');
  }
  if (!/[0-9]/.test(password)) {
    errors.push('숫자를 최소 1개 포함해야 합니다.');
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

export function validateUsername(username: string): boolean {
  const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/;
  return usernameRegex.test(username);
}
