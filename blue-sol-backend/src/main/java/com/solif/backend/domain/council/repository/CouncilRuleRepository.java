package com.solif.backend.domain.council.repository;

import com.solif.backend.domain.council.entity.Council;
import com.solif.backend.domain.council.entity.CouncilRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouncilRuleRepository extends JpaRepository<CouncilRule, Long> {

    // 자치회로 활동 규칙 목록 조회
    List<CouncilRule> findByCouncilOrderByCreatedAtAsc(Council council);
}