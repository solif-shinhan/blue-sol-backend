package com.solif.backend.global.common.response;

import org.springframework.http.HttpStatus;

public interface SuccessCode {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
