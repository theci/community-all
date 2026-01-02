package com.community.platform.reward.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserPoint is a Querydsl query type for UserPoint
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserPoint extends EntityPathBase<UserPoint> {

    private static final long serialVersionUID = -1981104111L;

    public static final QUserPoint userPoint = new QUserPoint("userPoint");

    public final com.community.platform.shared.domain.QAggregateRoot _super = new com.community.platform.shared.domain.QAggregateRoot(this);

    public final NumberPath<Integer> availablePoints = createNumber("availablePoints", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<UserLevel> currentLevel = createEnum("currentLevel", UserLevel.class);

    public final NumberPath<Integer> dailyEarnedPoints = createNumber("dailyEarnedPoints", Integer.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final DatePath<java.time.LocalDate> lastEarnedDate = createDate("lastEarnedDate", java.time.LocalDate.class);

    public final NumberPath<Integer> totalPoints = createNumber("totalPoints", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUserPoint(String variable) {
        super(UserPoint.class, forVariable(variable));
    }

    public QUserPoint(Path<? extends UserPoint> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserPoint(PathMetadata metadata) {
        super(UserPoint.class, metadata);
    }

}

