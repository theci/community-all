package com.community.platform.shared.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAggregateRoot is a Querydsl query type for AggregateRoot
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QAggregateRoot extends EntityPathBase<AggregateRoot> {

    private static final long serialVersionUID = 1777472855L;

    public static final QAggregateRoot aggregateRoot = new QAggregateRoot("aggregateRoot");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAggregateRoot(String variable) {
        super(AggregateRoot.class, forVariable(variable));
    }

    public QAggregateRoot(Path<? extends AggregateRoot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAggregateRoot(PathMetadata metadata) {
        super(AggregateRoot.class, metadata);
    }

}

