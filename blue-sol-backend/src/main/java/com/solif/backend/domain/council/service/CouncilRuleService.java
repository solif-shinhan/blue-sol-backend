package com.solif.backend.domain.council.service;

import com.solif.backend.domain.auth.exception.AuthErrorCode;
import com.solif.backend.domain.council.code.CouncilErrorCode;
import com.solif.backend.domain.council.dto.AddRuleResponse;
import com.solif.backend.domain.council.dto.RuleListResponse;
import com.solif.backend.domain.council.dto.RuleResponse;
import com.solif.backend.domain.council.entity.Council;
import com.solif.backend.domain.council.entity.CouncilMember;
import com.solif.backend.domain.council.entity.CouncilRule;
import com.solif.backend.domain.council.repository.CouncilMemberRepository;
import com.solif.backend.domain.council.repository.CouncilRepository;
import com.solif.backend.domain.council.repository.CouncilRuleRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilRuleService {

    private final CouncilRepository councilRepository;
    private final CouncilRuleRepository councilRuleRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final UserRepository userRepository;

    // 자치회 활동 규칙 목록 조회
    public RuleListResponse getRules(Long councilId) {
        log.info("자치회 활동 규칙 목록 조회 - councilId: {}", councilId);

        // 자치회 조회
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.COUNCIL_NOT_FOUND));

        // 규칙 목록 조회 (생성일 순)
        List<CouncilRule> rules = councilRuleRepository.findByCouncilOrderByCreatedAtAsc(council);

        // DTO 변환
        List<RuleResponse> ruleResponses = rules.stream()
                .map(RuleResponse::from)
                .collect(Collectors.toList());

        return RuleListResponse.of(councilId, ruleResponses);
    }

    // 자치회 활동 규칙 추가
    @Transactional
    public AddRuleResponse addRule(Long userId, Long councilId, String ruleContent) {
        log.info("자치회 활동 규칙 추가 - userId: {}, councilId: {}, ruleContent: {}", userId, councilId, ruleContent);

        // 사용자 조회
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 자치회 조회
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.COUNCIL_NOT_FOUND));

        // 리더 권한 확인
        validateLeaderAuthority(currentUser, council, CouncilErrorCode.ONLY_LEADER_CAN_ADD_RULE);

        // CouncilRule 생성 및 저장
        CouncilRule newRule = CouncilRule.builder()
                .council(council)
                .ruleContent(ruleContent)
                .build();

        CouncilRule savedRule = councilRuleRepository.save(newRule);

        // 총 규칙 수 조회
        Long totalRuleCount = councilRuleRepository.countByCouncil(council);

        return AddRuleResponse.of(
                councilId,
                savedRule.getCouncilRuleId(),
                savedRule.getRuleContent(),
                totalRuleCount.intValue()
        );
    }

    // 자치회 활동 규칙 삭제
    @Transactional
    public void deleteRule(Long userId, Long councilId, Long ruleId) {
        log.info("자치회 활동 규칙 삭제 - userId: {}, councilId: {}, ruleId: {}", userId, councilId, ruleId);

        // 사용자 조회
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 자치회 조회
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.COUNCIL_NOT_FOUND));

        // 리더 권한 확인
        validateLeaderAuthority(currentUser, council, CouncilErrorCode.ONLY_LEADER_CAN_DELETE_RULE);

        // 규칙 조회
        CouncilRule rule = councilRuleRepository.findById(ruleId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.RULE_NOT_FOUND));

        // 규칙이 해당 자치회의 것인지 확인
        if (!rule.getCouncil().getCouncilId().equals(councilId)) {
            throw new CustomException(CouncilErrorCode.RULE_NOT_FOUND);
        }

        // 규칙 삭제
        councilRuleRepository.delete(rule);
    }

    // 리더 권한 검증 (private helper method)
    private void validateLeaderAuthority(User user, Council council, CouncilErrorCode errorCode) {
        CouncilMember member = councilMemberRepository
                .findByCouncilAndUser(council, user)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.NOT_COUNCIL_MEMBER));

        if (!member.isLeader()) {
            throw new CustomException(errorCode);
        }
    }
}