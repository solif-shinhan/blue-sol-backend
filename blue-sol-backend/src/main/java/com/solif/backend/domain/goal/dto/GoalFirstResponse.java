package com.solif.backend.domain.goal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoalFirstResponse {

    private String firstGoal;
    private int currentIndex;
    private int totalCount;

    public static GoalFirstResponse of(String firstGoal, int currentIndex, int totalCount) {
        return GoalFirstResponse.builder()
                .firstGoal(firstGoal)
                .currentIndex(currentIndex)
                .totalCount(totalCount)
                .build();
    }
}
