package com.solif.backend.domain.council.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouncilSuccessCode implements SuccessCode {

    // 자치회 조회
    COUNCIL_LIST_SUCCESS(HttpStatus.OK, "COUNCIL_LIST_200", "자치회 목록 조회에 성공했습니다."),
    COUNCIL_DETAIL_SUCCESS(HttpStatus.OK, "COUNCIL_DETAIL_201", "자치회 상세 조회에 성공했습니다."),
    MY_COUNCIL_SUCCESS(HttpStatus.OK, "MY_COUNCIL_202", "내 자치회 조회에 성공했습니다."),

    // 자치회 생성/수정/삭제
    COUNCIL_CREATE_SUCCESS(HttpStatus.CREATED, "COUNCIL_CREATE_203", "자치회 생성에 성공했습니다."),
    COUNCIL_UPDATE_SUCCESS(HttpStatus.OK, "COUNCIL_UPDATE_204", "자치회 수정에 성공했습니다."),
    COUNCIL_DELETE_SUCCESS(HttpStatus.OK, "COUNCIL_DELETE_205", "자치회 삭제에 성공했습니다."),

    // 멤버 관리
    MEMBER_LIST_SUCCESS(HttpStatus.OK, "COUNCIL_206", "멤버 목록 조회에 성공했습니다."),
    MEMBER_ADD_SUCCESS(HttpStatus.OK, "COUNCIL_207", "멤버 추가에 성공했습니다."),
    MEMBER_DELETE_SUCCESS(HttpStatus.OK, "COUNCIL_208", "멤버 삭제에 성공했습니다."),

    // 활동 규칙 관리
    RULE_LIST_SUCCESS(HttpStatus.OK, "COUNCIL_209", "활동 규칙 목록 조회에 성공했습니다."),
    RULE_ADD_SUCCESS(HttpStatus.CREATED, "COUNCIL_210", "활동 규칙 추가에 성공했습니다."),
    RULE_DELETE_SUCCESS(HttpStatus.OK, "COUNCIL_211", "활동 규칙 삭제에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}