package com.solif.backend.domain.notification.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 유저별 SseEmitter를 인메모리로 관리하는 저장소.
 * Key: "userId_UUID" (한 유저가 여러 기기에서 접속 가능)
 *
 * 서버가 여러 대로 확장 시 Redis Pub/Sub으로 대체 가능.
 */
@Slf4j
@Repository
public class SseEmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    //Emitter 저장
    public SseEmitter save(String emitterId, SseEmitter emitter) {
        emitters.put(emitterId, emitter);
        log.info("SSE Emitter 저장 - emitterId: {}, 현재 연결 수: {}", emitterId, emitters.size());
        return emitter;
    }

    //Emitter 삭제
    public void deleteById(String emitterId) {
        emitters.remove(emitterId);
        log.info("SSE Emitter 삭제 - emitterId: {}, 현재 연결 수: {}", emitterId, emitters.size());
    }

    //특정 유저의 모든 Emitter 조회
    //Key가 "userId_" 로 시작하는 모든 Emitter 반환
    public Map<String, SseEmitter> findAllByUserId(Long userId) {
        String prefix = userId + "_";
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
