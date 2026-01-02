-- ============================================================
-- 테스트 데이터 초기화 스크립트
-- H2 Database 전용 (In-Memory)
-- ============================================================
-- 실행 방법:
-- 1. H2 콘솔 접속: http://3.39.36.234:8080/h2-console
-- 2. JDBC URL: jdbc:h2:mem:testdb
-- 3. Username: sa, Password: (비어있음)
-- 4. 이 스크립트를 복사해서 실행
-- ============================================================

-- 기존 데이터 삭제 (순서 중요 - 외래키 제약조건 고려)
DELETE FROM POST_SCRAPS;
DELETE FROM SCRAP_FOLDERS;
DELETE FROM POINT_TRANSACTIONS;
DELETE FROM USER_POINTS;
DELETE FROM POST_LIKES;
DELETE FROM COMMENTS;
DELETE FROM POST_TAGS;
DELETE FROM POSTS;
DELETE FROM CATEGORIES;
DELETE FROM USERS;

-- ============================================================
-- 1. 사용자 생성 (Users)
-- ============================================================
-- 비밀번호: Password123@ (BCrypt 인코딩)
-- BCrypt 해시는 Spring Security가 생성한 것과 호환됨

-- 일반 사용자 1
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, ROLE, STATUS, CREATED_AT, UPDATED_AT)
VALUES (1, 'test@test.com', '$2a$12$D6mZ.UueLQZbnUIFPEaEcOgf9eU9rkR92Q.i2VJN0NdBfW7tTvJG.', 'nick1', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 일반 사용자 2
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, ROLE, STATUS, CREATED_AT, UPDATED_AT)
VALUES (2, 'user2@test.com', '$2a$12$D6mZ.UueLQZbnUIFPEaEcOgf9eU9rkR92Q.i2VJN0NdBfW7tTvJG.', 'nick2', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 일반 사용자 3
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, ROLE, STATUS, CREATED_AT, UPDATED_AT)
VALUES (3, 'user3@test.com', '$2a$12$D6mZ.UueLQZbnUIFPEaEcOgf9eU9rkR92Q.i2VJN0NdBfW7tTvJG.', 'nick3', 'USER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ADMIN 사용자 (관리자)
INSERT INTO USERS (ID, EMAIL, PASSWORD, NICKNAME, ROLE, STATUS, CREATED_AT, UPDATED_AT)
VALUES (4, 'admin@test.com', '$2a$12$D6mZ.UueLQZbnUIFPEaEcOgf9eU9rkR92Q.i2VJN0NdBfW7tTvJG.', 'admin', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 2. 카테고리 생성 (Categories)
-- ============================================================
INSERT INTO CATEGORIES (ID, NAME, DESCRIPTION, IS_ACTIVE, DISPLAY_ORDER, CREATED_AT, UPDATED_AT)
VALUES (1, '자유게시판', '자유롭게 이야기를 나누는 공간', TRUE, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO CATEGORIES (ID, NAME, DESCRIPTION, IS_ACTIVE, DISPLAY_ORDER, CREATED_AT, UPDATED_AT)
VALUES (2, '질문&답변', '궁금한 점을 물어보세요', TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO CATEGORIES (ID, NAME, DESCRIPTION, IS_ACTIVE, DISPLAY_ORDER, CREATED_AT, UPDATED_AT)
VALUES (3, '개발', '개발 관련 정보 공유', TRUE, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO CATEGORIES (ID, NAME, DESCRIPTION, IS_ACTIVE, DISPLAY_ORDER, CREATED_AT, UPDATED_AT)
VALUES (4, '공지사항', '중요한 공지사항', TRUE, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 3. 게시글 생성 (Posts) - PUBLISHED 상태
-- ============================================================
INSERT INTO POSTS (ID, AUTHOR_ID, CATEGORY_ID, TITLE, CONTENT, CONTENT_TYPE, STATUS, IS_NOTICE_POST, VIEW_COUNT, LIKE_COUNT, COMMENT_COUNT, CREATED_AT, UPDATED_AT, PUBLISHED_AT)
VALUES (1, 1, 1, '첫 번째 게시글입니다', '안녕하세요! 첫 게시글입니다.\n\n## 소개\n이것은 마크다운 형식의 게시글입니다.', 'MARKDOWN', 'PUBLISHED', FALSE, 10, 5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POSTS (ID, AUTHOR_ID, CATEGORY_ID, TITLE, CONTENT, CONTENT_TYPE, STATUS, IS_NOTICE_POST, VIEW_COUNT, LIKE_COUNT, COMMENT_COUNT, CREATED_AT, UPDATED_AT, PUBLISHED_AT)
VALUES (2, 2, 2, 'Next.js에서 API 호출 방법', '# Next.js API 호출\n\n`fetch`를 사용하세요.\n\n```javascript\nconst res = await fetch("/api/posts");\n```', 'MARKDOWN', 'PUBLISHED', FALSE, 25, 8, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POSTS (ID, AUTHOR_ID, CATEGORY_ID, TITLE, CONTENT, CONTENT_TYPE, STATUS, IS_NOTICE_POST, VIEW_COUNT, LIKE_COUNT, COMMENT_COUNT, CREATED_AT, UPDATED_AT, PUBLISHED_AT)
VALUES (3, 1, 3, 'TypeScript 활용 팁', 'TypeScript를 사용하면 타입 안정성을 확보할 수 있습니다.', 'MARKDOWN', 'PUBLISHED', FALSE, 15, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POSTS (ID, AUTHOR_ID, CATEGORY_ID, TITLE, CONTENT, CONTENT_TYPE, STATUS, IS_NOTICE_POST, VIEW_COUNT, LIKE_COUNT, COMMENT_COUNT, CREATED_AT, UPDATED_AT, PUBLISHED_AT)
VALUES (4, 3, 1, '오늘 날씨 어때요?', '오늘 날씨가 참 좋네요! 다들 좋은 하루 보내세요.', 'MARKDOWN', 'PUBLISHED', FALSE, 8, 2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POSTS (ID, AUTHOR_ID, CATEGORY_ID, TITLE, CONTENT, CONTENT_TYPE, STATUS, IS_NOTICE_POST, VIEW_COUNT, LIKE_COUNT, COMMENT_COUNT, CREATED_AT, UPDATED_AT, PUBLISHED_AT)
VALUES (5, 2, 3, 'Docker 사용법 정리', '# Docker 기본 명령어\n\n- `docker ps`: 실행 중인 컨테이너 목록\n- `docker logs`: 로그 확인', 'MARKDOWN', 'PUBLISHED', FALSE, 30, 12, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POSTS (ID, AUTHOR_ID, CATEGORY_ID, TITLE, CONTENT, CONTENT_TYPE, STATUS, IS_NOTICE_POST, VIEW_COUNT, LIKE_COUNT, COMMENT_COUNT, CREATED_AT, UPDATED_AT, PUBLISHED_AT)
VALUES (6, 4, 4, '[공지] 서비스 점검 안내', '**2025년 12월 30일** 서비스 점검이 있을 예정입니다.\n\n점검 시간: 02:00 ~ 04:00 (2시간)', 'MARKDOWN', 'PUBLISHED', TRUE, 50, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 4. 댓글 생성 (Comments)
-- ============================================================
-- 게시글 1번의 댓글
INSERT INTO COMMENTS (ID, POST_ID, AUTHOR_ID, CONTENT, STATUS, LIKE_COUNT, DEPTH, CREATED_AT, UPDATED_AT)
VALUES (1, 1, 2, '좋은 게시글이네요!', 'ACTIVE', 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO COMMENTS (ID, POST_ID, AUTHOR_ID, PARENT_COMMENT_ID, CONTENT, STATUS, LIKE_COUNT, DEPTH, CREATED_AT, UPDATED_AT)
VALUES (2, 1, 3, 1, '저도 동감합니다!', 'ACTIVE', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 게시글 2번의 댓글
INSERT INTO COMMENTS (ID, POST_ID, AUTHOR_ID, CONTENT, STATUS, LIKE_COUNT, DEPTH, CREATED_AT, UPDATED_AT)
VALUES (3, 2, 1, '유용한 정보 감사합니다.', 'ACTIVE', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO COMMENTS (ID, POST_ID, AUTHOR_ID, CONTENT, STATUS, LIKE_COUNT, DEPTH, CREATED_AT, UPDATED_AT)
VALUES (4, 2, 3, 'axios도 사용 가능하나요?', 'ACTIVE', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 게시글 4번의 댓글
INSERT INTO COMMENTS (ID, POST_ID, AUTHOR_ID, CONTENT, STATUS, LIKE_COUNT, DEPTH, CREATED_AT, UPDATED_AT)
VALUES (5, 4, 1, '오늘 정말 날씨 좋네요!', 'ACTIVE', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO COMMENTS (ID, POST_ID, AUTHOR_ID, CONTENT, STATUS, LIKE_COUNT, DEPTH, CREATED_AT, UPDATED_AT)
VALUES (6, 4, 2, '산책하기 좋은 날씨예요', 'ACTIVE', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 5. 좋아요 (Post Likes)
-- ============================================================
-- 사용자 1이 게시글 2, 5번에 좋아요
INSERT INTO POST_LIKES (id, post_id, user_id, created_at, updated_at)
VALUES (1, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POST_LIKES (id, post_id, user_id, created_at, updated_at)
VALUES (2, 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 사용자 2가 게시글 1, 3, 5번에 좋아요
INSERT INTO POST_LIKES (id, post_id, user_id, created_at, updated_at)
VALUES (3, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POST_LIKES (id, post_id, user_id, created_at, updated_at)
VALUES (4, 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POST_LIKES (id, post_id, user_id, created_at, updated_at)
VALUES (5, 5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 사용자 3이 게시글 1, 2, 5번에 좋아요
INSERT INTO POST_LIKES (id, post_id, user_id, created_at, updated_at)
VALUES (6, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POST_LIKES (id, post_id, user_id, created_at, updated_at)
VALUES (7, 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POST_LIKES (id, post_id, user_id, created_at, updated_at)
VALUES (8, 5, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 6. 포인트 정보 (User Points)
-- ============================================================
INSERT INTO USER_POINTS (id, user_id, total_points, available_points, current_level, daily_earned_points, last_earned_date, created_at, updated_at)
VALUES (1, 1, 250, 240, 'LEVEL_2', 10, CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO USER_POINTS (id, user_id, total_points, available_points, current_level, daily_earned_points, last_earned_date, created_at, updated_at)
VALUES (2, 2, 150, 150, 'LEVEL_2', 5, CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO USER_POINTS (id, user_id, total_points, available_points, current_level, daily_earned_points, last_earned_date, created_at, updated_at)
VALUES (3, 3, 80, 80, 'LEVEL_1', 0, CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO USER_POINTS (id, user_id, total_points, available_points, current_level, daily_earned_points, last_earned_date, created_at, updated_at)
VALUES (4, 4, 1000, 1000, 'LEVEL_3', 0, CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 7. 포인트 거래 내역 (Point Transactions)
-- ============================================================
INSERT INTO POINT_TRANSACTIONS (id, user_id, transaction_type, points, balance_after, description, created_at, updated_at)
VALUES (1, 1, 'POST_CREATE', 10, 250, '게시글 작성', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POINT_TRANSACTIONS (id, user_id, transaction_type, points, balance_after, description, created_at, updated_at)
VALUES (2, 1, 'POINT_USE', -10, 240, '프로필 배경 구매', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POINT_TRANSACTIONS (id, user_id, transaction_type, points, balance_after, description, created_at, updated_at)
VALUES (3, 2, 'COMMENT_CREATE', 5, 150, '댓글 작성', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 8. 스크랩 폴더 (Scrap Folders)
-- ============================================================
-- 사용자 1의 기본 폴더
INSERT INTO SCRAP_FOLDERS (id, user_id, name, description, is_default, created_at, updated_at)
VALUES (1, 1, '기본 폴더', '기본 스크랩 폴더', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 사용자 1의 개발 자료 폴더
INSERT INTO SCRAP_FOLDERS (id, user_id, name, description, is_default, created_at, updated_at)
VALUES (2, 1, '개발 자료', '개발 관련 유용한 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 사용자 2의 기본 폴더
INSERT INTO SCRAP_FOLDERS (id, user_id, name, description, is_default, created_at, updated_at)
VALUES (3, 2, '기본 폴더', '기본 스크랩 폴더', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- 9. 스크랩 (Post Scraps)
-- ============================================================
-- 사용자 1이 게시글 2, 5번을 기본 폴더에 스크랩
INSERT INTO POST_SCRAPS (id, post_id, scrap_folder_id, user_id, created_at, updated_at)
VALUES (1, 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO POST_SCRAPS (id, post_id, scrap_folder_id, user_id, created_at, updated_at)
VALUES (2, 5, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 사용자 1이 게시글 3번을 개발 자료 폴더에 스크랩
INSERT INTO POST_SCRAPS (id, post_id, scrap_folder_id, user_id, created_at, updated_at)
VALUES (3, 3, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 사용자 2가 게시글 5번을 기본 폴더에 스크랩
INSERT INTO POST_SCRAPS (id, post_id, scrap_folder_id, user_id, created_at, updated_at)
VALUES (4, 5, 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================
-- AUTO_INCREMENT 시퀀스 재설정 (다음 INSERT를 위해)
-- ============================================================
ALTER TABLE USERS ALTER COLUMN id RESTART WITH 5;
ALTER TABLE CATEGORIES ALTER COLUMN id RESTART WITH 5;
ALTER TABLE POSTS ALTER COLUMN id RESTART WITH 7;
ALTER TABLE COMMENTS ALTER COLUMN id RESTART WITH 7;
ALTER TABLE POST_LIKES ALTER COLUMN id RESTART WITH 9;
ALTER TABLE USER_POINTS ALTER COLUMN id RESTART WITH 5;
ALTER TABLE POINT_TRANSACTIONS ALTER COLUMN id RESTART WITH 4;
ALTER TABLE SCRAP_FOLDERS ALTER COLUMN id RESTART WITH 4;
ALTER TABLE POST_SCRAPS ALTER COLUMN id RESTART WITH 5;

-- ============================================================
-- 완료 메시지
-- ============================================================
SELECT '테스트 데이터 초기화 완료!' AS MESSAGE;
SELECT '사용자 4명 (ID: 1~4)' AS USERS;
SELECT '  - test@test.com (ADMIN, nick1)' AS USER_1;
SELECT '  - user2@test.com (USER, nick2)' AS USER_2;
SELECT '  - user3@test.com (USER, nick3)' AS USER_3;
SELECT '  - admin@test.com (ADMIN, admin)' AS USER_4;
SELECT '비밀번호: Password123@' AS PASSWORD;
SELECT '카테고리 4개, 게시글 6개, 댓글 6개' AS DATA;
SELECT '좋아요, 스크랩, 포인트 데이터 포함' AS FEATURES;
