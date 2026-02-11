package com.solif.backend.domain.youtube.controller;

import com.solif.backend.domain.youtube.dto.VideoDto;
import com.solif.backend.domain.youtube.service.YouTubeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * YouTube 동영상 조회 API
 */
@Slf4j
@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
@Tag(name = "YouTube", description = "YouTube 동영상 조회 API")
public class YouTubeController {
    
    private final YouTubeService youtubeService;
    
    /**
     * 최신 동영상 목록 조회
     * 
     * GET /api/youtube/videos?maxResults=20
     */
    @GetMapping("/videos")
    @Operation(summary = "최신 동영상 목록 조회", description = "채널의 최신 동영상을 조회합니다.")
    public ResponseEntity<List<VideoDto>> getVideos(
        @Parameter(description = "최대 결과 개수 (1-50)", example = "20")
        @RequestParam(defaultValue = "20") Integer maxResults
    ) {
        log.info("동영상 목록 조회 요청: maxResults={}", maxResults);
        
        // 최대값 제한
        if (maxResults < 1) {
            maxResults = 1;
        }
        if (maxResults > 50) {
            maxResults = 50;
        }
        
        List<VideoDto> videos = youtubeService.getLatestVideos(maxResults);
        
        return ResponseEntity.ok(videos);
    }
    
    /**
     * 카테고리별 동영상 조회
     * 
     * GET /api/youtube/videos/category?category=인성&maxResults=10
     */
    @GetMapping("/videos/category")
    @Operation(summary = "카테고리별 동영상 조회", description = "특정 카테고리의 동영상을 조회합니다.")
    public ResponseEntity<List<VideoDto>> getVideosByCategory(
        @Parameter(description = "카테고리 (전체, 인성, 사회, 과학, 창업, 취업)", example = "인성")
        @RequestParam(required = false, defaultValue = "전체") String category,
        @Parameter(description = "최대 결과 개수 (1-50)", example = "20")
        @RequestParam(defaultValue = "20") Integer maxResults
    ) {
        log.info("카테고리별 동영상 조회: category={}, maxResults={}", category, maxResults);
        
        // 최대값 제한
        if (maxResults < 1) {
            maxResults = 1;
        }
        if (maxResults > 50) {
            maxResults = 50;
        }
        
        List<VideoDto> videos = youtubeService.getVideosByCategory(category, maxResults);
        
        return ResponseEntity.ok(videos);
    }
    
    /**
     * 캐시 수동 갱신 (관리자용)
     * 
     * POST /api/youtube/refresh
     */
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "캐시 수동 갱신", description = "YouTube 동영상 캐시를 수동으로 갱신합니다. (관리자용)")
    public ResponseEntity<String> refreshCache() {
        log.info("YouTube 캐시 수동 갱신 요청");
        
        youtubeService.refreshCache();
        
        return ResponseEntity.ok("캐시가 갱신되었습니다.");
    }
}
