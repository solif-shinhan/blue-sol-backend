package com.solif.backend.domain.goal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoalCountResponse {

    private int goalCount;

    public static GoalCountResponse of(int goalCount) {
        return GoalCountResponse.builder()
                .goalCount(goalCount)
                .build();
    }
}
