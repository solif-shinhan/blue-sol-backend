package com.solif.backend.domain.council.dto;

import com.solif.backend.domain.council.entity.Council;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CouncilListResponse {

    private CouncilMyResponse myCouncil;  // 본인 자치회 정보 (없으면 null)
    private List<CouncilItem> councils;   // 전체 자치회 리스트

    @Getter
    @Builder
    public static class CouncilItem {
        private Long councilId;
        private String councilName;
        private String region;
        private Long memberCount;
        private String profileImageUrl;

        public static CouncilItem from(Council council, Long memberCount) {
            return CouncilItem.builder()
                    .councilId(council.getCouncilId())
                    .councilName(council.getCouncilName())
                    .region(council.getRegion())
                    .memberCount(memberCount)
                    .profileImageUrl(null)  // 이미지 처리
                    .build();
        }
    }
}