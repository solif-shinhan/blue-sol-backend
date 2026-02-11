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
 * YouTube Data API 연동 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeService {
    
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
     * 채널의 전체 동영상 조회 (캐싱 적용 - 고정 키)
     * 최대 50개를 가져와서 캐싱하고, getLatestVideos에서 필요한 만큼만 반환
     */
    @Cacheable(value = "youtubeVideos", key = "'all'")
    private List<VideoDto> getAllVideos() {
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
            
            // 카테고리 분류 적용
            videos.forEach(this::assignCategory);
            
            return videos;
            
        } catch (Exception e) {
            log.error("YouTube API 호출 실패", e);
            throw new RuntimeException("YouTube 동영상 조회 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 채널의 최신 동영상 조회 (캐시된 전체 목록에서 필요한 만큼만 반환)
     * 
     * @param maxResults 최대 결과 개수 (1-50)
     * @return 동영상 목록
     */
    public List<VideoDto> getLatestVideos(Integer maxResults) {
        List<VideoDto> allVideos = getAllVideos();  // 캐시에서 가져옴 (key='all')
        
        // maxResults만큼만 자르기
        return allVideos.stream()
            .limit(maxResults)
            .collect(Collectors.toList());
    }
    
    /**
     * 카테고리별 동영상 필터링
     */
    public List<VideoDto> getVideosByCategory(String category, Integer maxResults) {
        List<VideoDto> allVideos = getLatestVideos(maxResults);
        
        if (category == null || category.equals("전체")) {
            return allVideos;
        }
        
        return allVideos.stream()
            .filter(video -> category.equals(video.getCategory()))
            .collect(Collectors.toList());
    }
    
    /**
     * 동영상 제목 기반 카테고리 자동 분류
     * 
     * 규칙:
     * 1. 제목에 [인성], [사회] 등 태그가 있으면 해당 카테고리
     * 2. 키워드 기반 분류
     */
    private void assignCategory(VideoDto video) {
        String title = video.getTitle().toLowerCase();
        
        // 1. 태그 기반 분류 (우선순위)
        if (title.contains("[인성]") || title.contains("[人性]")) {
            video.setCategory("인성");
        } else if (title.contains("[사회]") || title.contains("[社會]")) {
            video.setCategory("사회");
        } else if (title.contains("[과학]") || title.contains("[科學]")) {
            video.setCategory("과학");
        } else if (title.contains("[창업]") || title.contains("[創業]")) {
            video.setCategory("창업");
        } else if (title.contains("[취업]") || title.contains("[就業]")) {
            video.setCategory("취업");
        }
        // 2. 키워드 기반 분류
        else if (title.contains("인성") || title.contains("도덕") || title.contains("윤리")) {
            video.setCategory("인성");
        } else if (title.contains("사회") || title.contains("경제") || title.contains("정치")) {
            video.setCategory("사회");
        } else if (title.contains("과학") || title.contains("기술") || title.contains("ai") || 
                   title.contains("인공지능") || title.contains("우라만이")) {
            video.setCategory("과학");
        } else if (title.contains("창업") || title.contains("스타트업") || title.contains("기업가")) {
            video.setCategory("창업");
        } else if (title.contains("취업") || title.contains("면접") || title.contains("이력서") || 
                   title.contains("글로벌 금융")) {
            video.setCategory("취업");
        }
        // 기본값
        else {
            video.setCategory("전체");
        }
        
        // 강연자 추출
        extractSpeaker(video);
    }
    
    /**
     * 제목에서 강연자 이름 추출
     * 예: "어떤 삶을 살고 싶나요? 나를 아십니까? - 포이샤스 원종희 대표님"
     */
    private void extractSpeaker(VideoDto video) {
        String title = video.getTitle();
        
        // 패턴: "- XXX 대표님", "| XXX 교수님" 등
        String[] patterns = {" - ", " | ", " / "};
        
        for (String pattern : patterns) {
            if (title.contains(pattern)) {
                String[] parts = title.split(java.util.regex.Pattern.quote(pattern));
                if (parts.length > 1) {
                    String speakerPart = parts[parts.length - 1].trim();
                    video.setSpeaker(speakerPart);
                    break;
                }
            }
        }
    }
    
    /**
     * 캐시 수동 갱신용 메서드
     * 기존 캐시를 모두 지우고 새로 조회하여 캐시 갱신
     */
    @CacheEvict(value = "youtubeVideos", allEntries = true)
    public void refreshCache() {
        log.info("YouTube 동영상 캐시 갱신 시작");
        // 캐시를 지운 후 즉시 새로 조회하여 캐시 채우기
        getAllVideos();
        log.info("YouTube 동영상 캐시 갱신 완료");
    }
}
