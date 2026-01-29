package com.solif.backend.domain.network.dto;

import com.solif.backend.global.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NetworkSuccessCode implements SuccessCode {

    NETWORK_LIST_SUCCESS(HttpStatus.OK, "NETWORK_001", "교류망 목록 조회에 성공했습니다."),
    NETWORK_ADD_SUCCESS(HttpStatus.CREATED, "NETWORK_002", "교류망 추가에 성공했습니다."),
    NETWORK_INTERACTION_SUCCESS(HttpStatus.CREATED, "NETWORK_003", "상호작용 발송에 성공했습니다."),
    NETWORK_RECOMMENDATION_SUCCESS(HttpStatus.OK, "NETWORK_004", "교류망 추천 조회에 성공했습니다."),
    NETWORK_SEARCH_SUCCESS(HttpStatus.OK, "NETWORK_005", "교류망 검색에 성공했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
