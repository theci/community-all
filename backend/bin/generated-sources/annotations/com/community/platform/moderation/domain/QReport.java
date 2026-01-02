package com.community.platform.moderation.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReport is a Querydsl query type for Report
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReport extends EntityPathBase<Report> {

    private static final long serialVersionUID = 1904279135L;

    public static final QReport report = new QReport("report");

    public final com.community.platform.shared.domain.QAggregateRoot _super = new com.community.platform.shared.domain.QAggregateRoot(this);

    public final StringPath actionTaken = createString("actionTaken");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final EnumPath<ReportReason> reason = createEnum("reason", ReportReason.class);

    public final NumberPath<Long> reportedUserId = createNumber("reportedUserId", Long.class);

    public final NumberPath<Long> reporterId = createNumber("reporterId", Long.class);

    public final StringPath reviewComment = createString("reviewComment");

    public final DateTimePath<java.time.LocalDateTime> reviewedAt = createDateTime("reviewedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> reviewerId = createNumber("reviewerId", Long.class);

    public final EnumPath<ReportStatus> status = createEnum("status", ReportStatus.class);

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public final EnumPath<ReportTargetType> targetType = createEnum("targetType", ReportTargetType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReport(String variable) {
        super(Report.class, forVariable(variable));
    }

    public QReport(Path<? extends Report> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReport(PathMetadata metadata) {
        super(Report.class, metadata);
    }

}

