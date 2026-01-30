package com.solif.backend.domain.network.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "교류망 목록 조회 응답")
public class NetworkListResponse {

    @Schema(description = "내 역할", example = "SENIOR")
    private String myRole;

    @Schema(description = "총 교류 인원")
    private int totalCount;

    @Schema(description = "상단 아바타 리스트")
    private List<FriendSummary> addedFriends;

    @Schema(description = "상세 카드 리스트")
    private List<NetworkCard> networkCards;

    @Getter
    @Builder
    @Schema(description = "친구 요약 정보")
    public static class FriendSummary {
        @Schema(description = "사용자 ID")
        private Long userId;

        @Schema(description = "사용자 이름")
        private String userName;

        @Schema(description = "캐릭터")
        private String character;

        @Schema(description = "배경 패턴")
        private String backgroundPattern;
    }

    @Getter
    @Builder
    @Schema(description = "네트워크 카드 정보")
    public static class NetworkCard {
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

        @Schema(description = "목표 목록")
        private List<String> mainGoals;

        @Schema(description = "관심사 목록")
        private List<String> interests;

        @Schema(description = "버튼 타입", example = "CHEER")
        private String buttonType;
    }
}
