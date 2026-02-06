package com.solif.backend.global.jwt;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    //토큰을 블랙리스트에 추가
    public void addToBlacklist(String token, long expirationTime) {
        String key = BLACKLIST_PREFIX + token;
        long ttl = expirationTime - System.currentTimeMillis();

        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "logout", ttl, TimeUnit.MILLISECONDS);
            log.info("Token added to blacklist with TTL: {} ms", ttl);
        } else {
            log.warn("Token already expired, skipping blacklist addition");
        }
    }

    //토큰이 블랙리스트에 있는지 확인
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis connection error while checking blacklist", e);
            throw new CustomException(AuthErrorCode.REDIS_CONNECTION_ERROR);
        }
    }
}

