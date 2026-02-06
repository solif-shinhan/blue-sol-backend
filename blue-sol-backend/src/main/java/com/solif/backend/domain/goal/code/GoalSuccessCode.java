package com.solif.backend.domain.goal.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GoalSuccessCode implements SuccessCode {

    GOAL_FIRST_READ_SUCCESS(HttpStatus.OK, "GOAL_001", "첫 번째 목표 조회에 성공했습니다."),
    GOAL_COUNT_READ_SUCCESS(HttpStatus.OK, "GOAL_002", "목표 개수 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
