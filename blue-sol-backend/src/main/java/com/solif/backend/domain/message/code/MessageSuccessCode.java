package com.solif.backend.domain.message.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessageSuccessCode implements SuccessCode {

    MESSAGE_SEND_SUCCESS(HttpStatus.CREATED, "MESSAGE_001", "쪽지가 성공적으로 발송되었습니다."),
    MESSAGE_RECEIVED_LIST_SUCCESS(HttpStatus.OK, "MESSAGE_002", "받은 쪽지 목록 조회에 성공했습니다."),
    MESSAGE_SENT_LIST_SUCCESS(HttpStatus.OK, "MESSAGE_003", "보낸 쪽지 목록 조회에 성공했습니다."),
    MESSAGE_DETAIL_SUCCESS(HttpStatus.OK, "MESSAGE_004", "쪽지 상세 조회에 성공했습니다."),
    MESSAGE_DELETE_SUCCESS(HttpStatus.OK, "MESSAGE_005", "쪽지가 삭제되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
