package com.community.platform.content.infrastructure.persistence;

import com.community.platform.content.domain.Post;
import com.community.platform.content.domain.PostStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.community.platform.content.domain.QPost.post;
import static com.community.platform.content.domain.QCategory.category;
import static com.community.platform.content.domain.QPostTag.postTag;
import static com.community.platform.content.domain.QTag.tag;
import static com.community.platform.user.domain.QUser.user;

/**
 * QueryDSL을 사용한 커스텀 Repository 구현체
 * 복잡한 동적 쿼리와 성능 최적화된 검색 기능 제공
 */
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> searchPostsWithFilters(String keyword,
                                           String searchType,
                                           Long categoryId,
                                           List<String> tagNames,
                                           PostStatus status,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate,
                                           Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        // 키워드 검색 조건 (searchType에 따라 다른 조건)
        if (keyword != null && !keyword.trim().isEmpty()) {
            BooleanExpression searchCondition = null;
            String likePattern = "%" + keyword + "%";

            if (searchType == null || "ALL".equalsIgnoreCase(searchType)) {
                // 제목 + 내용 (MySQL은 기본적으로 case-insensitive)
                searchCondition = post.title.like(likePattern)
                        .or(post.content.like(likePattern));
            } else if ("TITLE".equalsIgnoreCase(searchType)) {
                // 제목만
                searchCondition = post.title.like(likePattern);
            } else if ("CONTENT".equalsIgnoreCase(searchType)) {
                // 내용만
                searchCondition = post.content.like(likePattern);
            } else if ("AUTHOR".equalsIgnoreCase(searchType)) {
                // 작성자 닉네임으로 검색 (User와 조인)
                builder.and(post.authorId.in(
                    queryFactory.select(user.id)
                        .from(user)
                        .where(user.nickname.like(likePattern)
                            .or(user.email.like(likePattern)))
                ));
            }

            if (searchCondition != null) {
                builder.and(searchCondition);
            }
        }

        // 카테고리 필터 조건
        if (categoryId != null) {
            builder.and(post.category.id.eq(categoryId));
        }

        // 태그 필터 조건
        if (tagNames != null && !tagNames.isEmpty()) {
            builder.and(post.id.in(
                queryFactory.select(postTag.post.id)
                    .from(postTag)
                    .join(postTag.tag, tag)
                    .where(tag.name.in(tagNames))
                    .groupBy(postTag.post.id)
                    .having(postTag.count().eq((long) tagNames.size()))
            ));
        }

        // 상태 조건
        if (status != null) {
            builder.and(post.status.eq(status));
        }

        // 날짜 범위 조건
        if (startDate != null && endDate != null) {
            builder.and(post.publishedAt.between(startDate, endDate));
        }

        JPAQuery<Post> query = queryFactory.selectFrom(post)
                .leftJoin(post.category, category).fetchJoin()
                .where(builder)
                .orderBy(post.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Post> posts = query.fetch();

        // 총 개수 조회
        Long total = queryFactory.select(post.count())
                .from(post)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(posts, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Post> findPopularPostsWithScore(PostStatus status, 
                                              LocalDateTime fromDate, 
                                              Pageable pageable) {
        
        // 인기도 점수 계산 (좋아요 * 3 + 댓글 * 2 + 조회수 * 1)
        NumberExpression<Long> popularityScore = post.likeCount.multiply(3)
                .add(post.commentCount.multiply(2))
                .add(post.viewCount);
        
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(post.status.eq(status));
        
        if (fromDate != null) {
            builder.and(post.publishedAt.goe(fromDate));
        }
        
        JPAQuery<Post> query = queryFactory.selectFrom(post)
                .leftJoin(post.category, category).fetchJoin()
                .where(builder)
                .orderBy(popularityScore.desc(), post.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        
        List<Post> posts = query.fetch();
        
        Long total = queryFactory.select(post.count())
                .from(post)
                .where(builder)
                .fetchOne();
        
        return new PageImpl<>(posts, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Post> findRecommendedPostsForUser(Long userId, 
                                                List<Long> followingUserIds, 
                                                List<String> interestedTags, 
                                                int limit) {
        
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(post.status.eq(PostStatus.PUBLISHED));
        builder.and(post.authorId.ne(userId)); // 본인 게시글 제외
        
        // 팔로잉 사용자의 게시글 우선
        BooleanExpression followingCondition = null;
        if (followingUserIds != null && !followingUserIds.isEmpty()) {
            followingCondition = post.authorId.in(followingUserIds);
        }
        
        // 관심 태그가 포함된 게시글
        BooleanExpression tagCondition = null;
        if (interestedTags != null && !interestedTags.isEmpty()) {
            tagCondition = post.id.in(
                queryFactory.select(postTag.post.id)
                    .from(postTag)
                    .join(postTag.tag, tag)
                    .where(tag.name.in(interestedTags))
            );
        }
        
        // 조건 조합
        if (followingCondition != null && tagCondition != null) {
            builder.and(followingCondition.or(tagCondition));
        } else if (followingCondition != null) {
            builder.and(followingCondition);
        } else if (tagCondition != null) {
            builder.and(tagCondition);
        }
        
        return queryFactory.selectFrom(post)
                .leftJoin(post.category, category).fetchJoin()
                .where(builder)
                .orderBy(post.publishedAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<Post> findTrendingPosts(PostStatus status, int hourRange, Pageable pageable) {
        
        LocalDateTime fromTime = LocalDateTime.now().minusHours(hourRange);
        
        // 최근 시간 내 활동이 많은 게시글 (최근 좋아요, 댓글 활동 기준)
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(post.status.eq(status));
        builder.and(post.publishedAt.goe(fromTime));
        
        // 최근 활동 점수 계산
        NumberExpression<Long> trendingScore = post.likeCount.add(post.commentCount.multiply(2));
        
        JPAQuery<Post> query = queryFactory.selectFrom(post)
                .leftJoin(post.category, category).fetchJoin()
                .where(builder)
                .orderBy(trendingScore.desc(), post.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        
        List<Post> posts = query.fetch();
        
        Long total = queryFactory.select(post.count())
                .from(post)
                .where(builder)
                .fetchOne();
        
        return new PageImpl<>(posts, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Object[]> getCategoryStatistics(PostStatus status, LocalDateTime fromDate) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(post.status.eq(status));

        if (fromDate != null) {
            builder.and(post.publishedAt.goe(fromDate));
        }

        return queryFactory.select(
                    category.id,
                    category.name,
                    post.count(),
                    post.likeCount.sum(),
                    post.commentCount.sum(),
                    post.viewCount.sum(),
                    post.likeCount.avg(),
                    post.commentCount.avg()
                )
                .from(post)
                .join(post.category, category)
                .where(builder)
                .groupBy(category.id, category.name)
                .orderBy(post.count().desc())
                .fetch()
                .stream()
                .map(tuple -> new Object[] {
                    tuple.get(category.id),
                    tuple.get(category.name),
                    tuple.get(post.count()),
                    tuple.get(post.likeCount.sum()),
                    tuple.get(post.commentCount.sum()),
                    tuple.get(post.viewCount.sum()),
                    tuple.get(post.likeCount.avg()),
                    tuple.get(post.commentCount.avg())
                })
                .toList();
    }

    @Override
    public Page<Post> searchPostsWithHighlight(String keyword, PostStatus status, Pageable pageable) {
        
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(post.status.eq(status));
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likePattern = "%" + keyword + "%";
            builder.and(post.title.like(likePattern)
                    .or(post.content.like(likePattern)));
        }
        
        JPAQuery<Post> query = queryFactory.selectFrom(post)
                .leftJoin(post.category, category).fetchJoin()
                .where(builder)
                .orderBy(post.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        
        List<Post> posts = query.fetch();
        
        Long total = queryFactory.select(post.count())
                .from(post)
                .where(builder)
                .fetchOne();
        
        return new PageImpl<>(posts, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Post> findSimilarPosts(Long postId, int limit) {
        
        // 해당 게시글의 태그 조회
        List<String> postTags = queryFactory.select(tag.name)
                .from(postTag)
                .join(postTag.tag, tag)
                .where(postTag.post.id.eq(postId))
                .fetch();
        
        if (postTags.isEmpty()) {
            return List.of();
        }
        
        // 유사한 태그를 가진 게시글 조회
        return queryFactory.selectFrom(post)
                .leftJoin(post.category, category).fetchJoin()
                .where(post.id.ne(postId)
                        .and(post.status.eq(PostStatus.PUBLISHED))
                        .and(post.id.in(
                                queryFactory.select(postTag.post.id)
                                        .from(postTag)
                                        .join(postTag.tag, tag)
                                        .where(tag.name.in(postTags))
                                        .groupBy(postTag.post.id)
                                        .orderBy(postTag.count().desc())
                        )))
                .orderBy(post.publishedAt.desc())
                .limit(limit)
                .fetch();
    }
}