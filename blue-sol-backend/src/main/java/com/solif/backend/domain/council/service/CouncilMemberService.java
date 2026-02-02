package com.solif.backend.domain.council.service;

import com.solif.backend.domain.auth.exception.AuthErrorCode;
import com.solif.backend.domain.council.code.CouncilErrorCode;
import com.solif.backend.domain.council.dto.AddMemberResponse;
import com.solif.backend.domain.council.dto.CouncilMemberResponse;
import com.solif.backend.domain.council.dto.MemberListResponse;
import com.solif.backend.domain.council.entity.Council;
import com.solif.backend.domain.council.entity.CouncilMember;
import com.solif.backend.domain.council.entity.CouncilMemberRole;
import com.solif.backend.domain.council.repository.CouncilMemberRepository;
import com.solif.backend.domain.council.repository.CouncilRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilMemberService {

    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final UserRepository userRepository;

    // 자치회 멤버 목록 조회
    public MemberListResponse getMembers(Long councilId) {
        log.info("자치회 멤버 목록 조회 - councilId: {}", councilId);

        // 자치회 조회
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.COUNCIL_NOT_FOUND));

        // 멤버 목록 조회 (JOIN FETCH로 N+1 방지)
        List<CouncilMember> members = councilMemberRepository
                .findByCouncilOrderByRoleDescJoinedAtAsc(council);

        // DTO 변환
        List<CouncilMemberResponse> memberResponses = members.stream()
                .map(CouncilMemberResponse::from)
                .collect(Collectors.toList());

        return MemberListResponse.of(councilId, memberResponses);
    }

    // 자치회 멤버 추가
    @Transactional
    public AddMemberResponse addMembers(Long userId, Long councilId, List<Long> userIdsToAdd) {
        log.info("자치회 멤버 추가 - userId: {}, councilId: {}, userIdsToAdd: {}", userId, councilId, userIdsToAdd);

        // 사용자 조회
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 자치회 조회
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.COUNCIL_NOT_FOUND));

        // 리더 권한 확인
        CouncilMember currentMember = councilMemberRepository
                .findByCouncilAndUser(council, currentUser)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.NOT_COUNCIL_MEMBER));

        if (!currentMember.isLeader()) {
            throw new CustomException(CouncilErrorCode.ONLY_LEADER_CAN_ADD_MEMBER);
        }

        // 중복 제거
        List<Long> uniqueUserIds = userIdsToAdd.stream()
                .distinct()
                .collect(Collectors.toList());

        if (uniqueUserIds.isEmpty()) {
            throw new CustomException(CouncilErrorCode.EMPTY_USER_IDS_TO_ADD);
        }

        // 사용자 조회
        List<User> usersToAdd = userRepository.findAllById(uniqueUserIds);

        // 존재하지 않는 사용자 검증
        if (usersToAdd.size() != uniqueUserIds.size()) {
            throw new CustomException(AuthErrorCode.USER_NOT_FOUND);
        }

        // 이미 자치회에 소속된 사용자 검증
        Set<Long> existingUserIds = councilMemberRepository.findUserIdsAlreadyInAnyCouncil(uniqueUserIds);
        if (!existingUserIds.isEmpty()) {
            throw new CustomException(CouncilErrorCode.ALREADY_IN_COUNCIL);
        }

        // CouncilMember 생성 및 저장
        List<CouncilMember> newMembers = usersToAdd.stream()
                .map(user -> CouncilMember.builder()
                        .user(user)
                        .council(council)
                        .role(CouncilMemberRole.MEMBER)
                        .build())
                .collect(Collectors.toList());

        try {
            councilMemberRepository.saveAll(newMembers);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 동시성 이슈로 인한 중복 추가 시도
            throw new CustomException(CouncilErrorCode.ALREADY_IN_COUNCIL);
        }

        // 총 멤버 수 조회
        Long totalMemberCount = councilMemberRepository.countByCouncil(council);

        // 알림 발송 (TODO: 나중에 구현)

        return AddMemberResponse.of(councilId, newMembers.size(), totalMemberCount.intValue());
    }

    // 자치회 멤버 삭제
    @Transactional
    public void deleteMember(Long userId, Long councilId, Long userIdToDelete) {
        log.info("자치회 멤버 삭제 - userId: {}, councilId: {}, userIdToDelete: {}", userId, councilId, userIdToDelete);

        // 사용자 조회
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 자치회 조회
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.COUNCIL_NOT_FOUND));

        // 리더 권한 확인
        CouncilMember currentMember = councilMemberRepository
                .findByCouncilAndUser(council, currentUser)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.NOT_COUNCIL_MEMBER));

        if (!currentMember.isLeader()) {
            throw new CustomException(CouncilErrorCode.ONLY_LEADER_CAN_DELETE_MEMBER);
        }

        // 본인 삭제 방지
        if (userId.equals(userIdToDelete)) {
            throw new CustomException(CouncilErrorCode.LEADER_CANNOT_DELETE_SELF);
        }

        // 삭제할 사용자 조회
        User userToDelete = userRepository.findById(userIdToDelete)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 삭제할 멤버가 자치회에 속해있는지 확인
        CouncilMember memberToDelete = councilMemberRepository
                .findByCouncilAndUser(council, userToDelete)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.NOT_COUNCIL_MEMBER));

        // 멤버 삭제
        councilMemberRepository.delete(memberToDelete);
    }
}