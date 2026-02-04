package com.solif.backend.domain.notification.controller;

import com.solif.backend.domain.notification.code.NotificationSuccessCode;
import com.solif.backend.domain.notification.dto.*;
import com.solif.backend.domain.notification.entity.NotificationCategory;
import com.solif.backend.domain.notification.entity.NotificationSubCategory;
import com.solif.backend.domain.notification.service.NotificationService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "알림", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    //  SSE 구독
    @Operation(
            summary = "SSE 실시간 알림 구독",
            description = "서버와 SSE 연결을 맺어 실시간 알림을 수신합니다. "
                    + "클라이언트는 EventSource 또는 fetch API로 연결합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Long userId) {
        return notificationService.subscribe(userId);
    }

    //  알림 목록 조회
    @Operation(
            summary = "알림 전체 목록 조회",
            description = "카테고리(공지사항/활동)와 필터(전체/안읽음)로 알림 목록을 조회합니다. "
                    + "활동 탭에서는 서브 카테고리(쪽지/교류/자치회 활동)로 추가 필터링 가능합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping
    public ResponseEntity<SuccessResponse<Slice<NotificationListResponse>>> getNotifications(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "카테고리 (NOTICE: 공지사항, ACTIVITY: 활동)")
            @RequestParam NotificationCategory category,
            @Parameter(description = "서브 카테고리 - 활동 탭 전용 (MESSAGE: 쪽지, NETWORK: 교류, COUNCIL: 자치회 활동)")
            @RequestParam(required = false) NotificationSubCategory subCategory,
            @Parameter(description = "필터 (ALL: 전체, UNREAD: 안읽음)")
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Slice<NotificationListResponse> response =
                notificationService.getNotifications(userId, category, subCategory, filter, pageable);
        return ResponseFactory.success(NotificationSuccessCode.NOTIFICATION_LIST_SUCCESS, response);
    }

    //  알림 읽음 처리
    @Operation(
            summary = "알림 읽음 처리",
            description = "특정 알림을 터치하여 상세 화면 진입 시 호출합니다. "
                    + "is_read를 true로 변경하고 read_at에 현재 시각을 기록합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<SuccessResponse<NotificationReadResponse>> markAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId
    ) {
        NotificationReadResponse response = notificationService.markAsRead(userId, notificationId);
        return ResponseFactory.success(NotificationSuccessCode.NOTIFICATION_READ_SUCCESS, response);
    }

    //  안읽은 알림 개수
    @Operation(
            summary = "안읽은 알림 개수 조회",
            description = "현재 사용자의 안읽은 알림 총 개수를 반환합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/unread-count")
    public ResponseEntity<SuccessResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal Long userId
    ) {
        UnreadCountResponse response = notificationService.getUnreadCount(userId);
        return ResponseFactory.success(NotificationSuccessCode.NOTIFICATION_UNREAD_COUNT_SUCCESS, response);
    }

    //  알림 상세 조회
    @Operation(
            summary = "알림 세부 조회",
            description = "알림의 상세 내용을 조회합니다. 이미지 첨부 리스트와 이동 버튼 정보를 포함합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{notificationId}")
    public ResponseEntity<SuccessResponse<NotificationDetailResponse>> getNotificationDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId
    ) {
        NotificationDetailResponse response =
                notificationService.getNotificationDetail(userId, notificationId);
        return ResponseFactory.success(NotificationSuccessCode.NOTIFICATION_DETAIL_SUCCESS, response);
    }
}
