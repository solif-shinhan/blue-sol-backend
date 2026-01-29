package com.solif.backend.domain.profile.dto;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileSuccessCode implements SuccessCode {

    PROFILE_CREATE_SUCCESS(HttpStatus.CREATED, "PROFILE_001", "프로필 생성에 성공했습니다."),
    PROFILE_READ_SUCCESS(HttpStatus.OK, "PROFILE_002", "프로필 조회에 성공했습니다."),
    PROFILE_UPDATE_SUCCESS(HttpStatus.OK, "PROFILE_003", "프로필 정보가 수정되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
