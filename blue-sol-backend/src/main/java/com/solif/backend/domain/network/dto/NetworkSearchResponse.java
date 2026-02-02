package com.solif.backend.domain.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "교류망 검색 응답")
public class NetworkSearchResponse {

    @Schema(description = "검색어")
    private String keyword;

    @Schema(description = "검색 결과 수")
    private int resultCount;

    @Schema(description = "검색된 사용자 목록")
    private List<SearchedUser> users;

    @Getter
    @Builder
    @Schema(description = "검색된 사용자 정보")
    public static class SearchedUser {
        @Schema(description = "사용자 ID")
        private Long userId;

        @Schema(description = "사용자 이름")
        private String userName;

        @Schema(description = "캐릭터")
        private String character;

        @Schema(description = "배경 패턴")
        private String backgroundPattern;

        @Schema(description = "SOLID 목표 이름")
        private String solidGoalName;

        @Schema(description = "관심사 목록")
        private List<String> interests;

        @Schema(description = "이미 연결 여부")
        @JsonProperty("isConnected")
        private boolean isConnected;

        @Schema(description = "자치회 소속 여부", example = "true")
        @JsonProperty("isInCouncil")
        private Boolean isInCouncil;

        @Schema(description = "소속 자치회 이름", example = "제주최강산한이들")
        private String councilName;
    }
}
