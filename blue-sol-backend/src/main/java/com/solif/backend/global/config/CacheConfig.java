package com.solif.backend.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class CacheConfig {

    @Value("${youtube.cache.ttl:3600}")
    private long cacheTtlSeconds;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("youtubeVideos");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10)                         // 최대 10개 항목 저장 (키가 'all' 하나만 사용)
                .expireAfterWrite(cacheTtlSeconds, TimeUnit.SECONDS)    // 설정된 시간 후 만료 (기본 1시간)
                .recordStats());                         // 통계 기록

        return cacheManager;
    }
}
