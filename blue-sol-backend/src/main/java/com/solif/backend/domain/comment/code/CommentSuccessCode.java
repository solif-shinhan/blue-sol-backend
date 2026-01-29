package com.solif.backend.domain.comment.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentSuccessCode implements SuccessCode {

    COMMENT_LIST_SUCCESS(HttpStatus.OK, "COMMENT_200", "댓글 목록 조회에 성공했습니다."),
    COMMENT_CREATE_SUCCESS(HttpStatus.CREATED, "COMMENT_201", "댓글 작성에 성공했습니다."),
    COMMENT_UPDATE_SUCCESS(HttpStatus.OK, "COMMENT_202", "댓글 수정에 성공했습니다."),
    COMMENT_DELETE_SUCCESS(HttpStatus.OK, "COMMENT_203", "댓글 삭제에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}