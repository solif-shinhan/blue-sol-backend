package com.solif.backend.domain.board.code;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BoardErrorCode implements ErrorCode {

    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_001", "게시판을 찾을 수 없습니다."),
    INVALID_BOARD_TYPE(HttpStatus.BAD_REQUEST, "BOARD_002", "유효하지 않은 게시판 타입입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}