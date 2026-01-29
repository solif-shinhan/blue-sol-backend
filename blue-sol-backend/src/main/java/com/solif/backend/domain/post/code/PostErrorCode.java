package com.solif.backend.domain.post.code;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_001", "게시글을 찾을 수 없습니다."),
    UNAUTHORIZED_POST_ACCESS(HttpStatus.FORBIDDEN, "POST_002", "게시글에 대한 권한이 없습니다."),
    DELETED_POST(HttpStatus.GONE, "POST_003", "삭제된 게시글입니다."),
    CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "POST_004", "해당 게시판은 카테고리가 필수입니다."),
    CATEGORY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "POST_005", "해당 게시판은 카테고리를 사용할 수 없습니다."),
    INVALID_CATEGORY_FOR_BOARD(HttpStatus.BAD_REQUEST, "POST_006", "해당 게시판에서 사용할 수 없는 카테고리입니다."),
    INVALID_BOARD(HttpStatus.BAD_REQUEST, "POST_007", "유효하지 않은 게시판입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}