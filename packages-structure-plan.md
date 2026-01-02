# Packages 재구조화 계획

## 새로운 구조

```
packages/
├── api-contract/              # API 클라이언트 (자동 생성)
│   ├── package.json
│   ├── src/
│   │   ├── client.ts         # axios 기반 클라이언트 (기존 apiClient 이동)
│   │   ├── generated/        # OpenAPI로 자동 생성될 영역
│   │   └── index.ts
│   └── tsconfig.json
│
├── types/                     # 공통 타입 정의
│   ├── package.json
│   ├── src/
│   │   ├── user.ts           # 기존 apps/web/lib/types/user.ts 이동
│   │   ├── post.ts
│   │   ├── comment.ts
│   │   └── index.ts
│   └── tsconfig.json
│
├── design-system/             # 공통 UI 컴포넌트
│   ├── package.json
│   ├── src/
│   │   ├── Button.tsx        # 기존 apps/web/components/ui/Button.tsx 이동
│   │   ├── Input.tsx
│   │   ├── Card.tsx
│   │   └── index.ts
│   └── tsconfig.json
│
└── utils/                     # 공통 유틸리티
    ├── package.json
    ├── src/
    │   ├── date.ts
    │   ├── format.ts
    │   └── index.ts
    └── tsconfig.json
```

## 마이그레이션 순서

1. ✅ api-contract 패키지 생성
2. ✅ types 패키지 생성
3. ✅ design-system 패키지 생성
4. ✅ utils 패키지 업데이트
5. ✅ 기존 코드 이동 및 import 경로 업데이트
