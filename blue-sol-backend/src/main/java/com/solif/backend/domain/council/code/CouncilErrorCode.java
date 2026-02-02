package com.solif.backend.domain.council.code;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouncilErrorCode implements ErrorCode {

    // 자치회 관련
    COUNCIL_NOT_FOUND(HttpStatus.NOT_FOUND, "COUNCIL_001", "존재하지 않는 자치회입니다."),

    // 멤버 관련
    NOT_COUNCIL_MEMBER(HttpStatus.FORBIDDEN, "COUNCIL_002", "해당 자치회의 멤버가 아닙니다."),
    ONLY_LEADER_CAN_UPDATE(HttpStatus.FORBIDDEN, "COUNCIL_003", "자치회 리더만 수정할 수 있습니다."),
    ONLY_LEADER_CAN_ADD_MEMBER(HttpStatus.FORBIDDEN, "COUNCIL_004", "자치회 리더만 멤버를 추가할 수 있습니다."),
    ONLY_LEADER_CAN_DELETE_MEMBER(HttpStatus.FORBIDDEN, "COUNCIL_005", "자치회 리더만 멤버를 삭제할 수 있습니다."),
    LEADER_CANNOT_DELETE_SELF(HttpStatus.BAD_REQUEST, "COUNCIL_006", "리더는 본인을 삭제할 수 없습니다."),
    EMPTY_USER_IDS_TO_ADD(HttpStatus.BAD_REQUEST, "COUNCIL_013", "추가할 사용자가 없습니다."),

    // 활동 규칙 관련
    RULE_NOT_FOUND(HttpStatus.NOT_FOUND, "COUNCIL_007", "존재하지 않는 활동 규칙입니다."),
    ONLY_LEADER_CAN_ADD_RULE(HttpStatus.FORBIDDEN, "COUNCIL_008", "자치회 리더만 활동 규칙을 추가할 수 있습니다."),
    ONLY_LEADER_CAN_DELETE_RULE(HttpStatus.FORBIDDEN, "COUNCIL_009", "자치회 리더만 활동 규칙을 삭제할 수 있습니다."),

    // 예산 관련
    INSUFFICIENT_BUDGET(HttpStatus.BAD_REQUEST, "COUNCIL_010", "예산이 부족합니다."),

    // 자치회 소속 관련
    ALREADY_IN_COUNCIL(HttpStatus.BAD_REQUEST, "COUNCIL_011", "이미 다른 자치회에 소속되어 있습니다."),
    BUDGET_REDUCTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COUNCIL_012", "사용한 예산보다 적게 총 예산을 설정할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}