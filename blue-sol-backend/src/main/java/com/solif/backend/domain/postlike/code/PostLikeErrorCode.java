package com.solif.backend.domain.postlike.code;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostLikeErrorCode implements ErrorCode {

    ALREADY_LIKED(HttpStatus.CONFLICT, "POST_LIKE_001", "이미 좋아요한 게시글입니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_LIKE_002", "좋아요를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}