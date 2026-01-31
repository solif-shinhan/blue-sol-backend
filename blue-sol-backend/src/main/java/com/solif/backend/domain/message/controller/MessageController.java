package com.solif.backend.domain.message.controller;

import com.solif.backend.domain.message.code.MessageSuccessCode;
import com.solif.backend.domain.message.dto.*;
import com.solif.backend.domain.message.service.MessageService;
import com.solif.backend.global.common.response.ResponseFactory;
import com.solif.backend.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "쪽지", description = "쪽지 API")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(
            summary = "쪽지 발송",
            description = "새로운 쪽지를 발송합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<SuccessResponse<MessageSendResponse>> sendMessage(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody MessageSendRequest request
    ) {
        MessageSendResponse response = messageService.sendMessage(userId, request);
        return ResponseFactory.success(MessageSuccessCode.MESSAGE_SEND_SUCCESS, response);
    }

    @Operation(
            summary = "받은 쪽지 목록 조회",
            description = "받은 쪽지 목록을 페이징하여 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/received")
    public ResponseEntity<SuccessResponse<Slice<MessageListResponse>>> getReceivedMessages(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Slice<MessageListResponse> messages = messageService.getReceivedMessages(userId, pageable);
        return ResponseFactory.success(MessageSuccessCode.MESSAGE_RECEIVED_LIST_SUCCESS, messages);
    }

    @Operation(
            summary = "보낸 쪽지 목록 조회",
            description = "보낸 쪽지 목록을 페이징하여 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/sent")
    public ResponseEntity<SuccessResponse<Slice<MessageListResponse>>> getSentMessages(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Slice<MessageListResponse> messages = messageService.getSentMessages(userId, pageable);
        return ResponseFactory.success(MessageSuccessCode.MESSAGE_SENT_LIST_SUCCESS, messages);
    }

    @Operation(
            summary = "쪽지 상세 조회",
            description = "쪽지 상세 내용을 조회합니다. 수신자가 조회 시 읽음 처리됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{messageId}")
    public ResponseEntity<SuccessResponse<MessageDetailResponse>> getMessageDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long messageId
    ) {
        MessageDetailResponse response = messageService.getMessageDetail(userId, messageId);
        return ResponseFactory.success(MessageSuccessCode.MESSAGE_DETAIL_SUCCESS, response);
    }

    @Operation(
            summary = "쪽지 삭제",
            description = "쪽지를 삭제합니다. 발신자/수신자 각각 삭제 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{messageId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMessage(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long messageId
    ) {
        messageService.deleteMessage(userId, messageId);
        return ResponseFactory.success(MessageSuccessCode.MESSAGE_DELETE_SUCCESS);
    }
}
