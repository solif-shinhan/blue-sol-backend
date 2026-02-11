package com.solif.backend.domain.youtube.service;

import com.solif.backend.domain.youtube.dto.VideoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * YouTube Data API 연동 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeService {
    
    private final YouTubeApiCacheService cacheService;
    
    /**
     * 채널의 최신 동영상 조회 (캐시된 전체 목록에서 필요한 만큼만 반환)
     * 
     * @param maxResults 최대 결과 개수 (1-50)
     * @return 동영상 목록
     */
    public List<VideoDto> getLatestVideos(Integer maxResults) {
        List<VideoDto> allVideos = cacheService.getAllVideos();  // 캐시 서비스에서 가져옴
        
        // 카테고리 분류 적용 (캐시된 데이터에는 아직 적용 안됨)
        allVideos.forEach(this::assignCategory);
        
        // maxResults만큼만 자르기
        return allVideos.stream()
            .limit(maxResults)
            .collect(Collectors.toList());
    }
    
    /**
     * 카테고리별 동영상 필터링
     * 전체 동영상 목록에서 카테고리 필터링 후 maxResults만큼 반환
     */
    public List<VideoDto> getVideosByCategory(String category, Integer maxResults) {
        // 전체 동영상 조회 (캐시에서 최대 50개)
        List<VideoDto> allVideos = cacheService.getAllVideos();
        
        // 카테고리 분류 적용
        allVideos.forEach(this::assignCategory);
        
        // "전체" 카테고리면 바로 maxResults만큼 반환
        if (category == null || category.equals("전체")) {
            return allVideos.stream()
                .limit(maxResults)
                .collect(Collectors.toList());
        }
        
        // 카테고리 필터링 먼저 → 그 다음 limit 적용
        return allVideos.stream()
            .filter(video -> category.equals(video.getCategory()))
            .limit(maxResults)  // ← 필터링 후 limit!
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
    public void refreshCache() {
        log.info("YouTube 동영상 캐시 갱신 시작");
        // 캐시를 지운 후 즉시 새로 조회하여 캐시 채우기
        cacheService.evictCache();
        cacheService.getAllVideos();
        log.info("YouTube 동영상 캐시 갱신 완료");
    }
}
