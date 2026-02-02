package com.solif.backend.domain.notification.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationSuccessCode implements SuccessCode {

    NOTIFICATION_LIST_SUCCESS(HttpStatus.OK, "NOTIFICATION_001", "알림 목록 조회에 성공했습니다."),
    NOTIFICATION_READ_SUCCESS(HttpStatus.OK, "NOTIFICATION_002", "알림이 읽음 처리되었습니다."),
    NOTIFICATION_UNREAD_COUNT_SUCCESS(HttpStatus.OK, "NOTIFICATION_003", "안읽은 알림 개수 조회 성공"),
    NOTIFICATION_DETAIL_SUCCESS(HttpStatus.OK, "NOTIFICATION_004", "알림 상세 조회에 성공했습니다."),
    NOTIFICATION_SUBSCRIBE_SUCCESS(HttpStatus.OK, "NOTIFICATION_005", "SSE 연결에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
