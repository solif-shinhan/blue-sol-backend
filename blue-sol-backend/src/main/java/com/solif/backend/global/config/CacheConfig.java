package com.solif.backend.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 캐시 설정
 * YouTube API 호출 결과를 캐싱하여 API 할당량을 절약
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("youtubeVideos");
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(100)                        // 최대 100개 항목 저장
            .expireAfterWrite(1, TimeUnit.HOURS)     // 1시간 후 만료
            .recordStats());                         // 통계 기록
        
        return cacheManager;
    }
}
