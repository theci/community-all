package com.community.platform.shared.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 설정 클래스
 * JPAQueryFactory 빈을 생성하여 QueryDSL 사용 환경 구성
 */
@Configuration
@RequiredArgsConstructor
public class QueryDslConfig {

    private final EntityManager entityManager;

    // JPAQueryFactory 빈 등록 (QueryDSL 쿼리 생성용)
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}