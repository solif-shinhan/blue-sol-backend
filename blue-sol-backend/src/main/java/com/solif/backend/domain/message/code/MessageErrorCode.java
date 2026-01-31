package com.solif.backend.domain.message.code;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessageErrorCode implements ErrorCode {

    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE_ERR_001", "쪽지를 찾을 수 없습니다."),
    UNAUTHORIZED_MESSAGE_ACCESS(HttpStatus.FORBIDDEN, "MESSAGE_ERR_002", "쪽지에 대한 접근 권한이 없습니다."),
    CANNOT_SEND_TO_SELF(HttpStatus.BAD_REQUEST, "MESSAGE_ERR_003", "자기 자신에게 쪽지를 보낼 수 없습니다."),
    RECEIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE_ERR_004", "받는 사람을 찾을 수 없습니다."),
    ALREADY_DELETED_MESSAGE(HttpStatus.GONE, "MESSAGE_ERR_005", "이미 삭제된 쪽지입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
