package com.solif.backend.domain.mission.repository;

import com.solif.backend.domain.mission.entity.Mission;
import com.solif.backend.domain.mission.entity.MissionCategory;
import com.solif.backend.domain.mission.entity.MissionConditionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findAllByOrderByMissionCategoryAscSequenceOrderAsc();

    List<Mission> findByMissionCategoryOrderBySequenceOrderAsc(MissionCategory category);

    Optional<Mission> findByMissionCategoryAndSequenceOrder(MissionCategory category, Integer sequenceOrder);

    // ConditionType으로 미션 조회
    Optional<Mission> findByConditionType(MissionConditionType conditionType);
}