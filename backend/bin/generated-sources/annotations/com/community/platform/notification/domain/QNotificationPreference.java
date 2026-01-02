package com.community.platform.notification.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationPreference is a Querydsl query type for NotificationPreference
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationPreference extends EntityPathBase<NotificationPreference> {

    private static final long serialVersionUID = 615313942L;

    public static final QNotificationPreference notificationPreference = new QNotificationPreference("notificationPreference");

    public final com.community.platform.shared.domain.QBaseEntity _super = new com.community.platform.shared.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath notifyComment = createBoolean("notifyComment");

    public final BooleanPath notifyLevelUp = createBoolean("notifyLevelUp");

    public final BooleanPath notifyLike = createBoolean("notifyLike");

    public final BooleanPath notifyPenalty = createBoolean("notifyPenalty");

    public final BooleanPath notifyReply = createBoolean("notifyReply");

    public final BooleanPath notifyReportResult = createBoolean("notifyReportResult");

    public final BooleanPath notifyScrap = createBoolean("notifyScrap");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QNotificationPreference(String variable) {
        super(NotificationPreference.class, forVariable(variable));
    }

    public QNotificationPreference(Path<? extends NotificationPreference> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationPreference(PathMetadata metadata) {
        super(NotificationPreference.class, metadata);
    }

}

