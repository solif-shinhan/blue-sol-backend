package com.solif.backend.domain.mission.event;

import com.solif.backend.domain.mission.entity.MissionConditionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 미션 체크 이벤트 : 트랜잭션 커밋 이후에 리스너가 수신하여 미션을 체크/완료 처리합니다.
@Getter
@AllArgsConstructor
public class MissionEvent {

    private final Long userId;
    private final MissionConditionType conditionType;
    private final Long sourceId;
}