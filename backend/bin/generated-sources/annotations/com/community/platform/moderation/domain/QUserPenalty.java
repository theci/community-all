package com.community.platform.moderation.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserPenalty is a Querydsl query type for UserPenalty
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserPenalty extends EntityPathBase<UserPenalty> {

    private static final long serialVersionUID = 1761506291L;

    public static final QUserPenalty userPenalty = new QUserPenalty("userPenalty");

    public final com.community.platform.shared.domain.QAggregateRoot _super = new com.community.platform.shared.domain.QAggregateRoot(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> createdBy = createNumber("createdBy", Long.class);

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isActive = createBoolean("isActive");

    public final EnumPath<PenaltyType> penaltyType = createEnum("penaltyType", PenaltyType.class);

    public final StringPath reason = createString("reason");

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUserPenalty(String variable) {
        super(UserPenalty.class, forVariable(variable));
    }

    public QUserPenalty(Path<? extends UserPenalty> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserPenalty(PathMetadata metadata) {
        super(UserPenalty.class, metadata);
    }

}

