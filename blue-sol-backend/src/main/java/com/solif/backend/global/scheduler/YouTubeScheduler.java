package com.solif.backend.global.scheduler;

import com.solif.backend.domain.youtube.service.YouTubeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * YouTube 관련 스케줄 작업
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YouTubeScheduler {
    
    private final YouTubeService youtubeService;
    
    /**
     * 1시간마다 자동으로 캐시 갱신
     * YouTube API 할당량을 고려하여 1시간 간격으로 설정
     * 
     * TODO: API 키 설정 후 주석 해제
     */
    // @Scheduled(fixedRate = 3600000)  // 1시간 = 3,600,000ms
    public void refreshYouTubeCache() {
        log.info("스케줄러: YouTube 캐시 자동 갱신 시작");
        try {
            youtubeService.refreshCache();
            log.info("스케줄러: YouTube 캐시 자동 갱신 완료");
        } catch (Exception e) {
            log.error("스케줄러: YouTube 캐시 갱신 실패", e);
        }
    }
}
