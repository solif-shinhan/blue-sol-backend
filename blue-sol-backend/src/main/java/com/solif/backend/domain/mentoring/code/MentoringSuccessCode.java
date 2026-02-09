package com.solif.backend.domain.mentoring.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MentoringSuccessCode implements SuccessCode {

    MENTOR_LIST_SUCCESS(HttpStatus.OK, "MENTORING_200", "전문가 멘토 목록 조회에 성공했습니다."),
    MENTORING_REQUEST_CREATE_SUCCESS(HttpStatus.CREATED, "MENTORING_201", "멘토링 신청에 성공했습니다."),
    MENTORING_REQUEST_SENT_LIST_SUCCESS(HttpStatus.OK, "MENTORING_202", "내가 보낸 신청서 목록 조회에 성공했습니다."),
    MENTORING_REQUEST_RECEIVED_LIST_SUCCESS(HttpStatus.OK, "MENTORING_203", "받은 신청서 목록 조회에 성공했습니다."),
    MENTORING_HOME_SUCCESS(HttpStatus.OK, "MENTORING_204", "멘토링 홈 조회에 성공했습니다."),
    MENTORING_CARD_SEND_SUCCESS(HttpStatus.CREATED, "MENTORING_205", "멘토링 엽서 발송에 성공했습니다."),
    MENTORING_CARD_SENT_LIST_SUCCESS(HttpStatus.OK, "MENTORING_206", "보낸 멘토링 엽서 목록 조회에 성공했습니다."),
    MENTORING_CARD_DETAIL_SUCCESS(HttpStatus.OK, "MENTORING_207", "멘토링 엽서 상세 조회에 성공했습니다."),
    MENTORING_REQUEST_DETAIL_SUCCESS(HttpStatus.OK, "MENTORING_208", "멘토링 신청서 상세 조회에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}