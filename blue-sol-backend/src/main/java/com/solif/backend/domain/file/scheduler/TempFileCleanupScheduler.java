package com.solif.backend.domain.file.scheduler;

import com.solif.backend.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TempFileCleanupScheduler {

    private final FileService fileService;

    private static final int TEMP_FILE_EXPIRY_HOURS = 24;

    /**
     * 매일 새벽 3시에 만료된 임시 파일 정리
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTempFiles() {
        log.info("=== 임시 파일 정리 스케줄러 시작 ===");

        try {
            int deletedCount = fileService.cleanupExpiredTempFiles(TEMP_FILE_EXPIRY_HOURS);
            log.info("=== 임시 파일 정리 완료: {}개 삭제 ===", deletedCount);
        } catch (Exception e) {
            log.error("=== 임시 파일 정리 중 오류 발생 ===", e);
        }
    }
}
