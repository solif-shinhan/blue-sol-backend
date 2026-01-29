package com.solif.backend.domain.goal.exception;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GoalErrorCode implements ErrorCode {

    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "GOAL_001", "프로필을 찾을 수 없습니다."),
    GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "GOAL_002", "목표가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
