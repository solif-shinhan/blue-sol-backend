package com.solif.backend.domain.youtube.dto;

import com.google.api.services.youtube.model.SearchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * YouTube 동영상 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    
    private String videoId;           // 유튜브 동영상 ID
    private String title;              // 제목
    private String description;        // 설명
    private String thumbnailUrl;       // 썸네일 URL (고화질)
    private String channelTitle;       // 채널명
    private LocalDateTime publishedAt; // 게시일
    private String videoUrl;           // 전체 URL
    
    // 커스텀 필드
    private String category;           // 카테고리 (인성, 사회, 과학, 창업, 취업)
    private String speaker;            // 강연자 이름
    
    /**
     * YouTube 검색 결과를 DTO로 변환
     */
    public static VideoDto fromSearchResult(SearchResult searchResult) {
        if (searchResult.getId() == null || searchResult.getId().getVideoId() == null) {
            return null;
        }

        var snippet = searchResult.getSnippet();
        if (snippet == null){
            return null;
        }

        String videoId = searchResult.getId().getVideoId();
        
        // publishedAt을 LocalDateTime으로 변환
        LocalDateTime publishedAt = null;
        if (snippet.getPublishedAt() != null) {
            publishedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(snippet.getPublishedAt().getValue()),
                ZoneId.of("Asia/Seoul")
            );
        }
        
        // 썸네일 URL 추출 (고화질 우선, 없으면 기본)
        String thumbnailUrl = null;
        if (snippet.getThumbnails() != null) {
            if (snippet.getThumbnails().getHigh() != null) {
                thumbnailUrl = snippet.getThumbnails().getHigh().getUrl();
            } else if (snippet.getThumbnails().getMedium() != null) {
                thumbnailUrl = snippet.getThumbnails().getMedium().getUrl();
            } else if (snippet.getThumbnails().getDefault() != null) {
                thumbnailUrl = snippet.getThumbnails().getDefault().getUrl();
            }
        }
        
        return VideoDto.builder()
                .videoId(videoId)
                .title(snippet.getTitle() != null ? snippet.getTitle() : "")  // ← null-safe
                .description(snippet.getDescription() != null ? snippet.getDescription() : "")
                .thumbnailUrl(thumbnailUrl)
                .channelTitle(snippet.getChannelTitle() != null ? snippet.getChannelTitle() : "")
                .publishedAt(publishedAt)
                .videoUrl("https://www.youtube.com/watch?v=" + videoId)
                .build();
    }
}
