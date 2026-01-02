import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// 인증이 필요한 경로들
const protectedRoutes = [
  '/posts/create',
  '/profile',
  '/scraps',
  '/ranking',
];

// 인증된 사용자는 접근할 수 없는 경로들 (로그인, 회원가입)
const authRoutes = ['/login', '/register'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // 토큰 확인 (쿠키 또는 헤더에서)
  const accessToken = request.cookies.get('accessToken')?.value;
  const isAuthenticated = !!accessToken;

  // 보호된 라우트에 인증 없이 접근 시도
  if (protectedRoutes.some(route => pathname.startsWith(route)) && !isAuthenticated) {
    const url = new URL('/login', request.url);
    url.searchParams.set('redirect', pathname);
    return NextResponse.redirect(url);
  }

  // 인증된 사용자가 로그인/회원가입 페이지 접근 시도
  if (authRoutes.some(route => pathname.startsWith(route)) && isAuthenticated) {
    return NextResponse.redirect(new URL('/', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};
