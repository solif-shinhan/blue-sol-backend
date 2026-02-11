package com.solif.backend.domain.mission.event;

import com.solif.backend.domain.mission.entity.MissionConditionType;
import com.solif.backend.domain.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 미션 이벤트 리스너 : 트랜잭션 커밋 후 미션 체크 로직을 실행합니다.
@Slf4j
@Component
@RequiredArgsConstructor
public class MissionEventListener {

    private final MissionService missionService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMissionEvent(MissionEvent event) {
        log.info(">>> 미션 이벤트 수신됨 - userId: {}, conditionType: {}, sourceId: {}",
                event.getUserId(), event.getConditionType(), event.getSourceId());
        try {
            // POST_VIEW는 incrementMissionProgress 사용
            if (event.getConditionType() == MissionConditionType.POST_VIEW) {
                missionService.incrementMissionProgress(event.getUserId(), event.getConditionType());
            } else {
                // 나머지는 checkAndCompleteMission 사용
                missionService.checkAndCompleteMission(
                        event.getUserId(),
                        event.getConditionType(),
                        event.getSourceId()
                );
            }
        } catch (Exception e) {
            log.warn("미션 체크 실패 - userId: {}, conditionType: {}, sourceId: {}",
                    +event.getUserId(), event.getConditionType(), event.getSourceId(), e);
        }
    }
}