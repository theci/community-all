# @ddd3/design-system

공통 UI 컴포넌트 및 디자인 시스템 라이브러리

## 포함된 컴포넌트

### Core Components (6개)
- `Button` - 다양한 variant와 size를 지원하는 버튼
- `Input` - 폼 입력 컴포넌트
- `Card` - 컨텐츠 카드 래퍼
- `Modal` - 모달 다이얼로그
- `Select` - 드롭다운 선택 컴포넌트
- `Textarea` - 텍스트 영역 입력 (문자 수 카운트, 자동 리사이즈 지원)

### Form Components (7개)
- `Checkbox` - 체크박스 입력
- `Radio` - 라디오 버튼 입력
- `RadioGroup` - 라디오 버튼 그룹
- `FormField` - 폼 필드 래퍼
- `FormLabel` - 폼 레이블 (필수/선택 표시 지원)
- `FormError` - 에러 메시지 표시
- `FormHelperText` - 헬퍼 텍스트 표시

### Feedback Components (3개)
- `Badge` / `BadgeDot` - 상태 배지 컴포넌트
- `Loading` / `LoadingSkeleton` - 로딩 인디케이터 및 스켈레톤
- `Tooltip` - 툴팁 컴포넌트 (4방향 위치 지원)

### Theme Components (1개)
- `ThemeToggle` - 다크/라이트 모드 토글 버튼

### Stores
- `useThemeStore` - Zustand 기반 테마 상태 관리 (localStorage 지속성)

## 사용 방법

```typescript
import {
  Button,
  Input,
  Card,
  Modal,
  Select,
  Textarea,
  Checkbox,
  Radio,
  RadioGroup,
  FormField,
  FormLabel,
  FormError,
  FormHelperText,
  Badge,
  BadgeDot,
  Loading,
  LoadingSkeleton,
  Tooltip,
  ThemeToggle,
  useThemeStore,
} from '@ddd3/design-system';

// 버튼 사용
<Button variant="primary" size="md">클릭</Button>

// 폼 필드 사용
<FormField>
  <FormLabel required>이메일</FormLabel>
  <Input type="email" placeholder="email@example.com" />
  <FormHelperText>이메일 주소를 입력하세요</FormHelperText>
</FormField>

// 테마 토글
<ThemeToggle />

// 테마 스토어 사용
const { theme, setTheme } = useThemeStore();
```

## 의존성

- `react` ^19.0.0
- `react-dom` ^19.0.0
- `zustand` ^5.0.2
- `tailwindcss` ^3.4.1

## 총 컴포넌트 수

**17개 컴포넌트 + 1개 스토어**
