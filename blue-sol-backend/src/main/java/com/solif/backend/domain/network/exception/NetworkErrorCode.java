package com.solif.backend.domain.network.exception;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NetworkErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "NETWORK_001", "사용자를 찾을 수 없습니다."),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "NETWORK_002", "프로필을 찾을 수 없습니다."),
    CONNECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "NETWORK_003", "이미 교류망에 추가된 사용자입니다."),
    SELF_CONNECTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "NETWORK_004", "자기 자신을 추가할 수 없습니다."),
    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "NETWORK_005", "교류 관계를 찾을 수 없습니다."),
    INVALID_QR_CODE(HttpStatus.BAD_REQUEST, "NETWORK_006", "유효하지 않은 QR 코드입니다."),
    INVALID_INTERACTION_TYPE(HttpStatus.BAD_REQUEST, "NETWORK_007", "유효하지 않은 상호작용 타입입니다."),
    INTERACTION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "NETWORK_008", "해당 상호작용을 보낼 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
