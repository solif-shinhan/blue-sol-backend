package com.solif.backend.domain.council.dto;

import com.solif.backend.domain.council.entity.Council;
import com.solif.backend.domain.council.entity.CouncilMemberRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CouncilDetailResponse {

    private Long councilId;
    private String councilName;
    private String region;
    private String activityCategory;
    private Long leaderUserId;
    private String leaderName;
    private String description;
    private Long totalBudget;
    private Long currentBudget;
    private Long memberCount;
    private Long activityCount;
    private Long monthsSinceCreation;
    private LocalDateTime createdAt;
    private String profileImageUrl;
    private String myRole;  // LEADER, MEMBER, NONE
    private Boolean isMember;

    // 본인 자치회용 (멤버/리더)
    public static CouncilDetailResponse forMember(
            Council council,
            Long memberCount,
            Long activityCount,
            Long monthsSinceCreation,
            CouncilMemberRole role
    ) {
        return CouncilDetailResponse.builder()
                .councilId(council.getCouncilId())
                .councilName(council.getCouncilName())
                .region(council.getRegion())
                .activityCategory(council.getActivityCategory())
                .leaderUserId(council.getLeader().getUserId())
                .leaderName(council.getLeader().getName())
                .description(council.getDescription())
                .totalBudget(council.getTotalBudget())
                .currentBudget(council.getCurrentBudget())
                .memberCount(memberCount)
                .activityCount(activityCount)
                .monthsSinceCreation(monthsSinceCreation)
                .createdAt(council.getCreatedAt())
                .profileImageUrl(null)  // 이미지 처리
                .myRole(role.name())
                .isMember(true)
                .build();
    }

    // 다른 자치회용 (비멤버)
    public static CouncilDetailResponse forNonMember(
            Council council,
            Long memberCount
    ) {
        return CouncilDetailResponse.builder()
                .councilId(council.getCouncilId())
                .councilName(council.getCouncilName())
                .region(council.getRegion())
                .activityCategory(council.getActivityCategory())
                .leaderUserId(council.getLeader().getUserId())
                .leaderName(council.getLeader().getName())
                .description(council.getDescription())
                .totalBudget(null)
                .currentBudget(null)
                .memberCount(memberCount)
                .activityCount(null)
                .monthsSinceCreation(null)
                .createdAt(council.getCreatedAt())
                .profileImageUrl(null)  // 이미지 처리
                .myRole("NONE")
                .isMember(false)
                .build();
    }
}