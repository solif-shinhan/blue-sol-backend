package com.solif.backend.domain.councilreview.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouncilReviewSuccessCode implements SuccessCode {

    // 활동 후기 조회
    COUNCIL_REVIEW_LIST_SUCCESS(HttpStatus.OK, "COUNCIL_REVIEW_200", "활동 후기 목록 조회에 성공했습니다."),
    COUNCIL_REVIEW_DETAIL_SUCCESS(HttpStatus.OK, "COUNCIL_REVIEW_201", "활동 후기 상세 조회에 성공했습니다."),

    // 활동 후기 생성/수정/삭제
    COUNCIL_REVIEW_CREATE_SUCCESS(HttpStatus.CREATED, "COUNCIL_REVIEW_202", "활동 후기 생성에 성공했습니다."),
    COUNCIL_REVIEW_UPDATE_SUCCESS(HttpStatus.OK, "COUNCIL_REVIEW_203", "활동 후기 수정에 성공했습니다."),
    COUNCIL_REVIEW_DELETE_SUCCESS(HttpStatus.OK, "COUNCIL_REVIEW_204", "활동 후기 삭제에 성공했습니다."),

    // 릴레이 작성/수정/삭제
    COUNCIL_REVIEW_RELAY_CREATE_SUCCESS(HttpStatus.CREATED, "COUNCIL_REVIEW_205", "릴레이 작성에 성공했습니다."),
    COUNCIL_REVIEW_RELAY_UPDATE_SUCCESS(HttpStatus.OK, "COUNCIL_REVIEW_206", "릴레이 수정에 성공했습니다."),
    COUNCIL_REVIEW_RELAY_DELETE_SUCCESS(HttpStatus.OK, "COUNCIL_REVIEW_207", "릴레이 삭제에 성공했습니다."),

    // 질문 조회
    COUNCIL_REVIEW_QUESTION_RANDOM_SUCCESS(HttpStatus.OK, "COUNCIL_REVIEW_208", "랜덤 질문 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}