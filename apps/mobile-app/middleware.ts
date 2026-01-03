import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

/**
 * Middleware for mobile-app
 * Handles device-based redirects
 */
export function middleware(request: NextRequest) {
  const userAgent = request.headers.get('user-agent') || '';
  const host = request.headers.get('host') || '';

  // Detect React Native WebView
  const isInApp = request.headers.get('x-requested-with') === 'ReactNativeWebView' ||
                  userAgent.includes('ReactNativeWebView');

  const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(userAgent);

  // Don't redirect inside app
  if (isInApp) {
    return NextResponse.next();
  }

  // Mobile accessing m. domain: OK
  // PC accessing m. domain: redirect to www.
  if (!isMobile && host.startsWith('m.')) {
    const url = request.nextUrl.clone();
    url.host = host.replace('m.', 'www.');

    return NextResponse.redirect(url, {
      status: 301, // Permanent redirect
      headers: {
        'Cache-Control': 'public, max-age=3600'
      }
    });
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};
