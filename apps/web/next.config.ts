import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: 'standalone',
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://3.39.36.234:8080/api/v1',
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://3.39.36.234:8080/api/:path*',
      },
    ];
  },
};

export default nextConfig;
