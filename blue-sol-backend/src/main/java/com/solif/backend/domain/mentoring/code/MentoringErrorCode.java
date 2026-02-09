package com.solif.backend.domain.mentoring.code;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MentoringErrorCode implements ErrorCode {

    MENTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTORING_001", "존재하지 않는 전문가 멘토입니다."),
    MENTORING_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTORING_002", "존재하지 않는 멘토링 신청입니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "MENTORING_003", "유효하지 않은 카테고리입니다."),
    INVALID_METHOD(HttpStatus.BAD_REQUEST, "MENTORING_004", "유효하지 않은 멘토링 방식입니다."),
    UNAUTHORIZED_REQUEST_ACCESS(HttpStatus.FORBIDDEN, "MENTORING_005", "해당 멘토링 신청에 대한 권한이 없습니다."),
    MENTORING_CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTORING_006", "존재하지 않는 멘토링 엽서입니다."),
    UNAUTHORIZED_CARD_ACCESS(HttpStatus.FORBIDDEN, "MENTORING_007", "해당 멘토링 엽서에 대한 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}