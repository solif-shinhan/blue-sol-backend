package com.solif.backend.domain.mission.code;

import com.solif.backend.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MissionErrorCode implements ErrorCode {

    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION_001", "미션을 찾을 수 없습니다."),
    USER_MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION_002", "사용자 미션을 찾을 수 없습니다."),
    MISSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "MISSION_003", "이미 완료된 미션입니다."),
    PINECONE_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION_004", "솔방울을 찾을 수 없습니다."),
    PINECONE_ALREADY_EARNED(HttpStatus.BAD_REQUEST, "MISSION_005", "이미 획득한 솔방울입니다."),
    CATEGORY_MISSIONS_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "MISSION_006", "해당 카테고리의 모든 미션을 완료해야 솔방울을 받을 수 있습니다."),
    INVALID_SEASON_KEY(HttpStatus.BAD_REQUEST, "MISSION_007", "유효하지 않은 시즌입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}