package com.solif.backend.domain.councilreview.service;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.council.repository.CouncilMemberRepository;
import com.solif.backend.domain.councilreview.code.CouncilReviewErrorCode;
import com.solif.backend.domain.councilreview.dto.request.CouncilReviewRelayCreateRequest;
import com.solif.backend.domain.councilreview.dto.request.CouncilReviewRelayUpdateRequest;
import com.solif.backend.domain.councilreview.dto.response.CouncilReviewRelayResponse;
import com.solif.backend.domain.councilreview.entity.CouncilReviewPost;
import com.solif.backend.domain.councilreview.entity.CouncilReviewQuestion;
import com.solif.backend.domain.councilreview.entity.CouncilReviewRelay;
import com.solif.backend.domain.councilreview.repository.CouncilReviewParticipantRepository;
import com.solif.backend.domain.councilreview.repository.CouncilReviewPostRepository;
import com.solif.backend.domain.councilreview.repository.CouncilReviewRelayRepository;
import com.solif.backend.domain.notification.entity.NotificationType;
import com.solif.backend.domain.notification.entity.NotificationTargetType;
import com.solif.backend.domain.notification.event.NotificationEvent;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilReviewRelayService {

    private final CouncilReviewRelayRepository relayRepository;
    private final CouncilReviewPostRepository postRepository;
    private final CouncilReviewParticipantRepository participantRepository;
    private final CouncilReviewQuestionService questionService;
    private final CouncilMemberRepository councilMemberRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 릴레이 후기 작성
    @Transactional
    public Map<String, Object> createRelay(Long userId, Long councilReviewPostId, CouncilReviewRelayCreateRequest request) {
        log.info("릴레이 작성 - userId: {}, postId: {}, questionId: {}", userId, councilReviewPostId, request.getQuestionId());

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 2. 활동 후기 조회
        CouncilReviewPost post = postRepository.findById(councilReviewPostId)
                .orElseThrow(() -> new CustomException(CouncilReviewErrorCode.COUNCIL_REVIEW_POST_NOT_FOUND));

        // 3. 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new CustomException(CouncilReviewErrorCode.COUNCIL_REVIEW_POST_DELETED);
        }

        // 4. 참여자 권한 확인
        boolean isParticipant = participantRepository.existsByCouncilReviewPostIdAndUserId(councilReviewPostId, userId);
        if (!isParticipant) {
            throw new CustomException(CouncilReviewErrorCode.NOT_PARTICIPANT);
        }

        // 5. 이미 작성했는지 확인 (1인 1회 제한)
        boolean alreadyWritten = relayRepository.existsByCouncilReviewPostIdAndWriterUserId(councilReviewPostId, userId);
        if (alreadyWritten) {
            throw new CustomException(CouncilReviewErrorCode.ALREADY_WRITTEN_RELAY);
        }

        // 6. 질문 검증 및 조회
        CouncilReviewQuestion question = questionService.validateAndGetQuestion(request.getQuestionId());

        // 7. 질문 중복 체크 (같은 활동 후기 내에서 중복 불가)
        List<Long> usedQuestionIds = relayRepository.findUsedQuestionIdsByPostId(councilReviewPostId);
        if (usedQuestionIds.contains(request.getQuestionId())) {
            throw new CustomException(CouncilReviewErrorCode.QUESTION_ALREADY_USED);
        }

        // 8. relay_order 자동 배정 (MAX + 1)
        Integer maxOrder = relayRepository.findMaxRelayOrderByPostId(councilReviewPostId);
        Integer newOrder = maxOrder + 1;

        // 9. 릴레이 생성
        CouncilReviewRelay relay = CouncilReviewRelay.builder()
                .councilReviewPost(post)
                .councilReviewQuestion(question)
                .writerUser(user)
                .relayOrder(newOrder)
                .relayContent(request.getRelayContent())
                .build();

        CouncilReviewRelay savedRelay = relayRepository.save(relay);

        log.info("릴레이 작성 완료 - relayId: {}, relayOrder: {}", savedRelay.getCouncilReviewRelayId(), newOrder);

        // 활동 완료 체크: 모든 참여자가 릴레이 작성을 완료했는지 확인
        long participantCount = participantRepository.countByCouncilReviewPost_CouncilReviewPostId(councilReviewPostId);
        long relayCount = relayRepository.countByCouncilReviewPost_CouncilReviewPostId(councilReviewPostId);

        if (relayCount >= participantCount) {
            // 모든 참여자 작성 완료 → 팀원 전체에게 활동 완료 알림
            Long councilId = post.getCouncil().getCouncilId();
            List<Long> memberUserIds = councilMemberRepository.findUserIdsByCouncilId(councilId);
            String postTitle = post.getPost().getPostTitle();

            for (Long memberId : memberUserIds) {
                eventPublisher.publishEvent(new NotificationEvent(
                        memberId,
                        NotificationType.COUNCIL_ACTIVITY_DONE,
                        NotificationTargetType.COUNCIL_POST,
                        councilReviewPostId,
                        "[" + postTitle + "] 작성이 완료되었어요!",
                        "[" + postTitle + "] 작성이 완료되었어요! 완성된 글을 확인해 보세요."
                ));
            }
        }

        return Map.of(
                "councilReviewRelayId", savedRelay.getCouncilReviewRelayId(),
                "relayOrder", newOrder
        );
    }

    // 릴레이 수정
    @Transactional
    public void updateRelay(Long userId, Long relayId, CouncilReviewRelayUpdateRequest request) {
        log.info("릴레이 수정 - userId: {}, relayId: {}", userId, relayId);

        // 1. 릴레이 조회
        CouncilReviewRelay relay = relayRepository.findByIdWithWriterAndPost(relayId)
                .orElseThrow(() -> new CustomException(CouncilReviewErrorCode.RELAY_NOT_FOUND));

        // 2. 작성자 권한 확인
        if (!relay.isWriter(userId)) {
            throw new CustomException(CouncilReviewErrorCode.ONLY_WRITER_CAN_UPDATE_RELAY);
        }

        // 3. 릴레이 내용 수정
        relay.updateRelayContent(request.getRelayContent());

        log.info("릴레이 수정 완료 - relayId: {}", relayId);
    }

    // 릴레이 삭제
    @Transactional
    public void deleteRelay(Long userId, Long relayId) {
        log.info("릴레이 삭제 - userId: {}, relayId: {}", userId, relayId);

        // 1. 릴레이 조회
        CouncilReviewRelay relay = relayRepository.findByIdWithWriterAndPost(relayId)
                .orElseThrow(() -> new CustomException(CouncilReviewErrorCode.RELAY_NOT_FOUND));

        // 2. 작성자 권한 확인
        if (!relay.isWriter(userId)) {
            throw new CustomException(CouncilReviewErrorCode.ONLY_WRITER_CAN_DELETE_RELAY);
        }

        Long postId = relay.getCouncilReviewPost().getCouncilReviewPostId();

        // 3. 릴레이 Hard Delete
        relayRepository.delete(relay);

        // 4. relay_order 재정렬
        reorderRelays(postId);

        log.info("릴레이 삭제 완료 - relayId: {}", relayId);
    }

    /**
     * relay_order 재정렬
     * 릴레이 삭제 후 순서를 1, 2, 3, ... 으로 재정렬
     */
    private void reorderRelays(Long postId) {
        log.info("relay_order 재정렬 시작 - postId: {}", postId);

        List<CouncilReviewRelay> relays = relayRepository.findByCouncilReviewPostIdOrderByRelayOrder(postId);

        // 순서 재배정
        for (int i = 0; i < relays.size(); i++) {
            relays.get(i).updateRelayOrder(i + 1);
        }

        relayRepository.saveAll(relays);

        log.info("relay_order 재정렬 완료 - postId: {}, 총 릴레이 수: {}", postId, relays.size());
    }

    // 특정 활동 후기의 모든 릴레이 조회 (상세 조회용)
    public List<CouncilReviewRelayResponse> getRelaysByPostId(Long postId, Long currentUserId) {
        log.info("릴레이 목록 조회 - postId: {}", postId);

        List<CouncilReviewRelay> relays = relayRepository.findByCouncilReviewPostIdWithWriterAndQuestion(postId);

        return relays.stream()
                .map(relay -> CouncilReviewRelayResponse.from(relay, currentUserId))
                .collect(Collectors.toList());
    }
}