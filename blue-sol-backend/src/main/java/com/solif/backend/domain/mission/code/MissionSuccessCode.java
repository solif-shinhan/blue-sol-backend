package com.solif.backend.domain.mission.code;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MissionSuccessCode implements SuccessCode {

    MISSION_LIST_SUCCESS(HttpStatus.OK, "MISSION_200", "미션 목록 조회에 성공했습니다."),
    MISSION_PROGRESS_SUCCESS(HttpStatus.OK, "MISSION_201", "미션 진행 상황 조회에 성공했습니다."),
    PINECONE_EARNED_SUCCESS(HttpStatus.OK, "MISSION_202", "솔방울 획득에 성공했습니다."),
    PINECONE_MEMORY_SUCCESS(HttpStatus.OK, "MISSION_203", "솔방울 추억 조회에 성공했습니다."),
    MISSION_COMPLETED(HttpStatus.OK, "MISSION_204", "미션을 완료했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}