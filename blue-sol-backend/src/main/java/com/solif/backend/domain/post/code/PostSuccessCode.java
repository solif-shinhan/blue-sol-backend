package com.solif.backend.domain.post.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostSuccessCode implements SuccessCode {

    POST_LIST_SUCCESS(HttpStatus.OK, "POST_200", "게시글 목록 조회에 성공했습니다."),
    POST_DETAIL_SUCCESS(HttpStatus.OK, "POST_201", "게시글 상세 조회에 성공했습니다."),
    POST_CREATE_SUCCESS(HttpStatus.CREATED, "POST_202", "게시글 작성에 성공했습니다."),
    POST_UPDATE_SUCCESS(HttpStatus.OK, "POST_203", "게시글 수정에 성공했습니다."),
    POST_DELETE_SUCCESS(HttpStatus.OK, "POST_204", "게시글 삭제에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}