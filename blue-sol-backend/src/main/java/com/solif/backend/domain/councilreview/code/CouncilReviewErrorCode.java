package com.solif.backend.domain.councilreview.code;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouncilReviewErrorCode implements ErrorCode {

    // 활동 후기 관련
    COUNCIL_REVIEW_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "COUNCIL_REVIEW_001", "존재하지 않는 활동 후기입니다."),
    COUNCIL_REVIEW_POST_DELETED(HttpStatus.BAD_REQUEST, "COUNCIL_REVIEW_002", "삭제된 활동 후기입니다."),

    // 권한 관련 (활동 후기)
    ONLY_LEADER_CAN_CREATE_REVIEW(HttpStatus.FORBIDDEN, "COUNCIL_REVIEW_003", "활동 후기 작성 권한이 없습니다. 리더만 작성 가능합니다."),
    ONLY_LEADER_CAN_UPDATE_REVIEW(HttpStatus.FORBIDDEN, "COUNCIL_REVIEW_004", "활동 후기 수정 권한이 없습니다. 리더만 수정 가능합니다."),
    ONLY_LEADER_CAN_DELETE_REVIEW(HttpStatus.FORBIDDEN, "COUNCIL_REVIEW_005", "활동 후기 삭제 권한이 없습니다. 리더만 삭제 가능합니다."),

    // 릴레이 관련
    RELAY_NOT_FOUND(HttpStatus.NOT_FOUND, "COUNCIL_REVIEW_008", "존재하지 않는 릴레이입니다."),
    ONLY_WRITER_CAN_UPDATE_RELAY(HttpStatus.FORBIDDEN, "COUNCIL_REVIEW_009", "릴레이 수정 권한이 없습니다. 작성자만 수정 가능합니다."),
    ONLY_WRITER_CAN_DELETE_RELAY(HttpStatus.FORBIDDEN, "COUNCIL_REVIEW_010", "릴레이 삭제 권한이 없습니다. 작성자만 삭제 가능합니다."),

    // 릴레이 작성 제약
    NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "COUNCIL_REVIEW_006", "릴레이 작성 권한이 없습니다. 참여자만 작성 가능합니다."),
    ALREADY_WRITTEN_RELAY(HttpStatus.BAD_REQUEST, "COUNCIL_REVIEW_007", "이미 릴레이를 작성했습니다. 1인 1회만 작성 가능합니다."),

    // 질문 관련
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "COUNCIL_REVIEW_011", "존재하지 않는 질문입니다."),
    QUESTION_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "COUNCIL_REVIEW_012", "비활성화된 질문입니다."),
    QUESTION_ALREADY_USED(HttpStatus.BAD_REQUEST, "COUNCIL_REVIEW_013", "이미 사용된 질문입니다. 중복 사용할 수 없습니다."),
    NO_AVAILABLE_QUESTIONS(HttpStatus.BAD_REQUEST, "COUNCIL_REVIEW_016", "사용 가능한 질문이 없습니다."),

    // 참여자 관련
    PARTICIPANT_NOT_COUNCIL_MEMBER(HttpStatus.BAD_REQUEST, "COUNCIL_REVIEW_014", "참여자 중 자치회 멤버가 아닌 사용자가 있습니다."),
    PARTICIPANT_LIST_EMPTY(HttpStatus.BAD_REQUEST, "COUNCIL_REVIEW_015", "참여자 명단이 비어있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}