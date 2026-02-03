package com.solif.backend.domain.file.exception;

import lombok.Getter;

@Getter
public class FileException extends RuntimeException {

    private final FileErrorCode errorCode;

    public FileException(FileErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public enum FileErrorCode {
        FILE_NOT_FOUND("파일을 찾을 수 없습니다"),
        FILE_ATTACHMENT_NOT_FOUND("파일 첨부 정보를 찾을 수 없습니다"),
        FILE_UPLOAD_FAILED("파일 업로드에 실패했습니다"),
        INVALID_FILE_TYPE("지원하지 않는 파일 형식입니다"),
        FILE_SIZE_EXCEEDED("파일 크기가 제한을 초과했습니다");

        private final String message;

        FileErrorCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
