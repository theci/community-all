package com.community.platform.reward.application;

import com.community.platform.reward.domain.PointTransaction;
import com.community.platform.reward.domain.UserPoint;
import com.community.platform.reward.dto.PointTransactionResponse;
import com.community.platform.reward.dto.UserPointResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Reward 도메인 Mapper
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RewardMapper {

    /**
     * UserPoint -> UserPointResponse 변환
     */
    @Mapping(target = "levelDisplayName", expression = "java(userPoint.getCurrentLevel().getDisplayName())")
    @Mapping(target = "levelNumber", expression = "java(userPoint.getCurrentLevel().getLevelNumber())")
    @Mapping(target = "pointsToNextLevel", expression = "java(userPoint.getPointsToNextLevel())")
    @Mapping(target = "remainingDailyLimit", expression = "java(userPoint.getRemainingDailyLimit())")
    UserPointResponse toUserPointResponse(UserPoint userPoint);

    /**
     * PointTransaction -> PointTransactionResponse 변환
     */
    @Mapping(target = "transactionTypeDescription", expression = "java(transaction.getTransactionType().getDescription())")
    PointTransactionResponse toPointTransactionResponse(PointTransaction transaction);
}
