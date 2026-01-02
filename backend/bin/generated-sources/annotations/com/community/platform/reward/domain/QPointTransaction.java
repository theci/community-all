package com.community.platform.reward.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPointTransaction is a Querydsl query type for PointTransaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPointTransaction extends EntityPathBase<PointTransaction> {

    private static final long serialVersionUID = -1881008606L;

    public static final QPointTransaction pointTransaction = new QPointTransaction("pointTransaction");

    public final com.community.platform.shared.domain.QBaseEntity _super = new com.community.platform.shared.domain.QBaseEntity(this);

    public final NumberPath<Long> adminId = createNumber("adminId", Long.class);

    public final NumberPath<Integer> balanceAfter = createNumber("balanceAfter", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Integer> points = createNumber("points", Integer.class);

    public final NumberPath<Long> referenceId = createNumber("referenceId", Long.class);

    public final StringPath referenceType = createString("referenceType");

    public final EnumPath<PointTransactionType> transactionType = createEnum("transactionType", PointTransactionType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QPointTransaction(String variable) {
        super(PointTransaction.class, forVariable(variable));
    }

    public QPointTransaction(Path<? extends PointTransaction> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPointTransaction(PathMetadata metadata) {
        super(PointTransaction.class, metadata);
    }

}

