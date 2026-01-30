package com.solif.backend.domain.council.service;

import com.solif.backend.domain.auth.exception.AuthErrorCode;
import com.solif.backend.domain.council.code.CouncilErrorCode;
import com.solif.backend.domain.council.dto.*;
import com.solif.backend.domain.council.entity.Council;
import com.solif.backend.domain.council.entity.CouncilMember;
import com.solif.backend.domain.council.entity.CouncilMemberRole;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilService {

    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final CouncilRuleRepository councilRuleRepository;
    private final UserRepository userRepository;

    // 자치회 목록 조회
    public CouncilListResponse getCouncils(Long userId) {
        log.info("자치회 목록 조회 - userId: {}", userId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 내 자치회 조회
        CouncilMyResponse myCouncil = getMyCouncil(userId);

        // 전체 자치회 목록 조회
        List<Council> councils = councilRepository.findAll();

        List<CouncilListResponse.CouncilItem> councilItems = councils.stream()
                .map(council -> {
                    Long memberCount = councilMemberRepository.countByCouncil(council);
                    return CouncilListResponse.CouncilItem.from(council, memberCount);
                })
                .collect(Collectors.toList());

        return CouncilListResponse.builder()
                .myCouncil(myCouncil)  // 있으면 정보, 없으면 null
                .councils(councilItems)
                .build();
    }

    // 내 자치회 조회 (홈 화면용)
    public CouncilMyResponse getMyCouncil(Long userId) {
        log.info("내 자치회 조회 - userId: {}", userId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 사용자의 자치회 멤버십 조회
        CouncilMember myMembership = councilMemberRepository.findByUser(user)
                .orElse(null);

        if (myMembership == null) {
            return null;  // 소속 자치회 없음
        }

        // 자치회 조회
        Council council = myMembership.getCouncil();

        // 멤버 수 조회
        Long memberCount = councilMemberRepository.countByCouncil(council);

        return CouncilMyResponse.from(council, memberCount, myMembership.getRole());
    }

    // 자치회 상세 조회
    public CouncilDetailResponse getCouncilDetail(Long userId, Long councilId) {
        log.info("자치회 상세 조회 - councilId: {}, userId: {}", councilId, userId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 자치회 조회
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.COUNCIL_NOT_FOUND));

        // 현재 사용자의 역할 확인
        CouncilMember myMembership = councilMemberRepository
                .findByCouncilAndUser(council, user)
                .orElse(null);

        // 멤버 수 조회
        Long memberCount = councilMemberRepository.countByCouncil(council);

        // 본인 자치회인 경우
        if (myMembership != null) {
            // 활동 후기 수 조회 (나중에 구현)
            Long activityCount = 0L;

            // 생성 이후 개월 수 계산
            Long monthsSinceCreation = ChronoUnit.MONTHS.between(
                    council.getCreatedAt().toLocalDate().withDayOfMonth(1),
                    LocalDateTime.now().toLocalDate().withDayOfMonth(1)
            );

            return CouncilDetailResponse.forMember(
                    council,
                    memberCount,
                    activityCount,
                    monthsSinceCreation,
                    myMembership.getRole()
            );
        } else {
            // 다른 자치회인 경우
            return CouncilDetailResponse.forNonMember(council, memberCount);
        }
    }

    // 자치회 생성
    @Transactional
    public CouncilCreateResponse createCouncil(Long userId, CouncilCreateRequest request) {
        log.info("자치회 생성 - userId: {}, councilName: {}", userId, request.getCouncilName());

        // 사용자 조회
        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 자치회 생성
        Council council = Council.builder()
                .leader(leader)
                .councilName(request.getCouncilName())
                .region(request.getRegion())
                .activityCategory(request.getActivityCategory())
                .description(request.getDescription())
                .totalBudget(request.getTotalBudget())
                .profileImageFileId(request.getProfileImageFileId())
                .build();

        Council savedCouncil = councilRepository.save(council);

        // 리더를 CouncilMember에 LEADER로 등록
        CouncilMember leaderMember = CouncilMember.builder()
                .user(leader)
                .council(savedCouncil)
                .role(CouncilMemberRole.LEADER)
                .build();

        councilMemberRepository.save(leaderMember);

        // 초대된 멤버들 MEMBER로 등록
        int addedMemberCount = 0;
        if (request.getMemberUserIds() != null && !request.getMemberUserIds().isEmpty()) {
            List<User> members = userRepository.findAllById(request.getMemberUserIds());

            List<CouncilMember> councilMembers = members.stream()
                    .map(member -> CouncilMember.builder()
                            .user(member)
                            .council(savedCouncil)
                            .role(CouncilMemberRole.MEMBER)
                            .build())
                    .collect(Collectors.toList());

            councilMemberRepository.saveAll(councilMembers);
            addedMemberCount = councilMembers.size();
        }

        // 활동 규칙 추가
        if (request.getRules() != null && !request.getRules().isEmpty()) {
            List<CouncilRule> rules = request.getRules().stream()
                    .map(ruleContent -> CouncilRule.builder()
                            .council(savedCouncil)
                            .ruleContent(ruleContent)
                            .build())
                    .collect(Collectors.toList());

            councilRuleRepository.saveAll(rules);
        }

        // 알림 발송

        return CouncilCreateResponse.from(savedCouncil, addedMemberCount + 1);
    }

    // 자치회 수정
    @Transactional
    public void updateCouncil(Long userId, Long councilId, CouncilUpdateRequest request) {
        log.info("자치회 수정 - userId: {}, councilId: {}", userId, councilId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 자치회 조회
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.COUNCIL_NOT_FOUND));

        // 리더 권한 확인
        CouncilMember member = councilMemberRepository
                .findByCouncilAndUser(council, user)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.NOT_COUNCIL_MEMBER));

        if (!member.isLeader()) {
            throw new CustomException(CouncilErrorCode.ONLY_LEADER_CAN_UPDATE);
        }

        // 자치회 수정
        council.updateCouncil(
                request.getCouncilName(),
                request.getRegion(),
                request.getActivityCategory(),
                request.getDescription(),
                request.getTotalBudget(),
                request.getProfileImageFileId()
        );
    }
}