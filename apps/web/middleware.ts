import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

/**
 * Middleware for web app
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

  // Check if host is an IP address (don't redirect for IP addresses)
  const isIpAddress = /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}/.test(host);

  // Don't redirect for localhost or IP addresses
  if (isIpAddress || host.includes('localhost') || host.includes('127.0.0.1')) {
    return NextResponse.next();
  }

  // PC accessing www. domain: OK
  // Mobile accessing www. domain: redirect to m.
  if (isMobile && (host.startsWith('www.') || !host.includes('m.'))) {
    const url = request.nextUrl.clone();
    const newHost = host.startsWith('www.')
      ? host.replace('www.', 'm.')
      : 'm.' + host;
    url.host = newHost;

    return NextResponse.redirect(url, {
      status: 302, // Temporary redirect (mobile app install prompt possible)
    });
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};
