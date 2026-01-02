#!/bin/bash

# Scraps 페이지
sed -i \
  -e 's/"min-h-screen bg-gray-50/"min-h-screen bg-gray-50 dark:bg-gray-900/g' \
  -e 's/"text-3xl font-bold text-gray-900/"text-3xl font-bold text-gray-900 dark:text-white/g' \
  -e 's/"text-gray-600"/"text-gray-600 dark:text-gray-300"/g' \
  -e 's/"text-gray-500"/"text-gray-500 dark:text-gray-400"/g' \
  -e 's/"bg-gray-50 "/"bg-gray-50 dark:bg-gray-800 "/g' \
  -e 's/"text-gray-900 "/"text-gray-900 dark:text-white "/g' \
  -e 's/"font-semibold text-gray-900"/"font-semibold text-gray-900 dark:text-white"/g' \
  -e 's/"text-sm text-gray-600"/"text-sm text-gray-600 dark:text-gray-400"/g' \
  -e 's/"text-lg font-semibold text-gray-900/"text-lg font-semibold text-gray-900 dark:text-white/g' \
  -e 's/"text-xl font-semibold text-gray-900"/"text-xl font-semibold text-gray-900 dark:text-white"/g' \
  -e 's/"bg-red-50 /"bg-red-50 dark:bg-red-900\/20 /g' \
  -e 's/ text-red-700/ text-red-700 dark:text-red-400/g' \
  -e 's/ text-red-600/ text-red-600 dark:text-red-400/g' \
  "/home/ec2-user/DDD2/frontend/app/(main)/scraps/page.tsx"

# Search 페이지
sed -i \
  -e 's/"min-h-screen bg-gray-50/"min-h-screen bg-gray-50 dark:bg-gray-900/g' \
  -e 's/"text-2xl font-bold text-gray-900/"text-2xl font-bold text-gray-900 dark:text-white/g' \
  -e 's/"text-sm text-gray-600"/"text-sm text-gray-600 dark:text-gray-300"/g' \
  -e 's/"text-lg font-semibold text-gray-900/"text-lg font-semibold text-gray-900 dark:text-white/g' \
  -e 's/"text-xl font-semibold text-gray-900/"text-xl font-semibold text-gray-900 dark:text-white/g' \
  -e 's/"text-gray-500"/"text-gray-500 dark:text-gray-400"/g' \
  "/home/ec2-user/DDD2/frontend/app/(main)/search/page.tsx"

# Login 페이지
sed -i \
  -e 's/"min-h-screen flex items-center justify-center bg-gray-50/"min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900/g' \
  -e 's/"text-3xl font-bold text-gray-900"/"text-3xl font-bold text-gray-900 dark:text-white"/g' \
  -e 's/"text-sm text-gray-600"/"text-sm text-gray-600 dark:text-gray-300"/g' \
  "/home/ec2-user/DDD2/frontend/app/(auth)/login/page.tsx"

# Register 페이지
sed -i \
  -e 's/"min-h-screen flex items-center justify-center bg-gray-50/"min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900/g' \
  -e 's/"text-3xl font-bold text-gray-900"/"text-3xl font-bold text-gray-900 dark:text-white"/g' \
  -e 's/"text-sm text-gray-600"/"text-sm text-gray-600 dark:text-gray-300"/g' \
  "/home/ec2-user/DDD2/frontend/app/(auth)/register/page.tsx"

# Auth Layout
sed -i \
  -e 's/from-blue-50 to-indigo-100"/from-blue-50 to-indigo-100 dark:from-gray-800 dark:to-gray-900"/g' \
  "/home/ec2-user/DDD2/frontend/app/(auth)/layout.tsx"

echo "다크모드 클래스 추가 완료!"
