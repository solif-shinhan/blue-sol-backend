package com.solif.backend.domain.postlike.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostLikeSuccessCode implements SuccessCode {

    LIKE_SUCCESS(HttpStatus.CREATED, "POST_LIKE_200", "좋아요를 추가했습니다."),
    UNLIKE_SUCCESS(HttpStatus.OK, "POST_LIKE_201", "좋아요를 취소했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}