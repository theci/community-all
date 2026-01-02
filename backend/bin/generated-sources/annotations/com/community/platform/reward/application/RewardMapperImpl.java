package com.community.platform.reward.application;

import com.community.platform.reward.domain.PointTransaction;
import com.community.platform.reward.domain.UserPoint;
import com.community.platform.reward.dto.PointTransactionResponse;
import com.community.platform.reward.dto.UserPointResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-23T11:21:06+0000",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class RewardMapperImpl implements RewardMapper {

    @Override
    public UserPointResponse toUserPointResponse(UserPoint userPoint) {
        if ( userPoint == null ) {
            return null;
        }

        UserPointResponse.UserPointResponseBuilder userPointResponse = UserPointResponse.builder();

        userPointResponse.availablePoints( userPoint.getAvailablePoints() );
        userPointResponse.createdAt( userPoint.getCreatedAt() );
        userPointResponse.currentLevel( userPoint.getCurrentLevel() );
        userPointResponse.dailyEarnedPoints( userPoint.getDailyEarnedPoints() );
        userPointResponse.id( userPoint.getId() );
        userPointResponse.lastEarnedDate( userPoint.getLastEarnedDate() );
        userPointResponse.totalPoints( userPoint.getTotalPoints() );
        userPointResponse.userId( userPoint.getUserId() );

        userPointResponse.levelDisplayName( userPoint.getCurrentLevel().getDisplayName() );
        userPointResponse.levelNumber( userPoint.getCurrentLevel().getLevelNumber() );
        userPointResponse.pointsToNextLevel( userPoint.getPointsToNextLevel() );
        userPointResponse.remainingDailyLimit( userPoint.getRemainingDailyLimit() );

        return userPointResponse.build();
    }

    @Override
    public PointTransactionResponse toPointTransactionResponse(PointTransaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        PointTransactionResponse.PointTransactionResponseBuilder pointTransactionResponse = PointTransactionResponse.builder();

        pointTransactionResponse.adminId( transaction.getAdminId() );
        pointTransactionResponse.balanceAfter( transaction.getBalanceAfter() );
        pointTransactionResponse.createdAt( transaction.getCreatedAt() );
        pointTransactionResponse.description( transaction.getDescription() );
        pointTransactionResponse.id( transaction.getId() );
        pointTransactionResponse.points( transaction.getPoints() );
        pointTransactionResponse.referenceId( transaction.getReferenceId() );
        pointTransactionResponse.referenceType( transaction.getReferenceType() );
        pointTransactionResponse.transactionType( transaction.getTransactionType() );
        pointTransactionResponse.userId( transaction.getUserId() );

        pointTransactionResponse.transactionTypeDescription( transaction.getTransactionType().getDescription() );

        return pointTransactionResponse.build();
    }
}
