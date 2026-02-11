package com.solif.backend.domain.youtube.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.solif.backend.domain.youtube.dto.VideoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * YouTube API 캐시 전용 서비스
 * Spring AOP 프록시가 정상 동작하도록 별도 클래스로 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeApiCacheService {
    
    @Value("${youtube.api.key}")
    private String apiKey;
    
    @Value("${youtube.api.channel-id}")
    private String channelId;
    
    /**
     * YouTube API 클라이언트 생성
     */
    private YouTube getYouTubeService() throws GeneralSecurityException, IOException {
        return new YouTube.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            null
        )
        .setApplicationName("blue-sol-backend")
        .build();
    }
    
    /**
     * 채널의 전체 동영상 조회 (캐싱 적용)
     * 최대 50개를 가져와서 캐싱
     * 
     * @return 동영상 목록 (최대 50개)
     */
    @Cacheable(value = "youtubeVideos", key = "'all'")
    public List<VideoDto> getAllVideos() {
        try {
            log.info("YouTube API 호출: 전체 동영상 조회 (maxResults=50)");
            
            YouTube youtube = getYouTubeService();
            
            // 검색 요청 생성
            YouTube.Search.List search = youtube.search()
                .list(java.util.Arrays.asList("id", "snippet"))
                .setChannelId(channelId)
                .setKey(apiKey)
                .setMaxResults(50L)  // 최대 50개 가져오기
                .setOrder("date")  // 최신순
                .setType(java.util.Arrays.asList("video")); // 동영상만
            
            // API 호출
            SearchListResponse response = search.execute();
            List<SearchResult> searchResults = response.getItems();
            if (searchResults == null) {
                return List.of();
            }
            
            // DTO 변환
            List<VideoDto> videos = searchResults.stream()
                .map(VideoDto::fromSearchResult)
                .filter(video -> video != null)  // null 제외
                .collect(Collectors.toList());
            
            log.info("YouTube API 응답: {} 개의 동영상 조회됨", videos.size());
            
            return videos;
            
        } catch (Exception e) {
            log.error("YouTube API 호출 실패", e);
            throw new RuntimeException("YouTube 동영상 조회 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 캐시 삭제 (캐시 갱신 시 사용)
     */
    @CacheEvict(value = "youtubeVideos", allEntries = true)
    public void evictCache() {
        log.info("YouTube 동영상 캐시 삭제 완료");
    }
}
