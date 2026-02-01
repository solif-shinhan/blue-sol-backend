package com.solif.backend.domain.notification.code;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_ERR_001", "알림을 찾을 수 없습니다."),
    UNAUTHORIZED_NOTIFICATION_ACCESS(HttpStatus.FORBIDDEN, "NOTIFICATION_ERR_002", "알림에 대한 접근 권한이 없습니다."),
    SSE_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "NOTIFICATION_ERR_003", "실시간 알림 연결에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
