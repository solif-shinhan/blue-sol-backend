package com.solif.backend.domain.councilreview.service;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.board.code.BoardErrorCode;
import com.solif.backend.domain.board.entity.Board;
import com.solif.backend.domain.board.repository.BoardRepository;
import com.solif.backend.domain.comment.repository.CommentRepository;
import com.solif.backend.domain.council.code.CouncilErrorCode;
import com.solif.backend.domain.council.entity.Council;
import com.solif.backend.domain.council.entity.CouncilMember;
import com.solif.backend.domain.council.repository.CouncilMemberRepository;
import com.solif.backend.domain.council.repository.CouncilRepository;
import com.solif.backend.domain.councilreview.code.CouncilReviewErrorCode;
import com.solif.backend.domain.councilreview.dto.request.CouncilReviewPostCreateRequest;
import com.solif.backend.domain.councilreview.dto.request.CouncilReviewPostUpdateRequest;
import com.solif.backend.domain.councilreview.dto.response.*;
import com.solif.backend.domain.councilreview.entity.CouncilReviewParticipant;
import com.solif.backend.domain.councilreview.entity.CouncilReviewPost;
import com.solif.backend.domain.councilreview.entity.CouncilReviewQuestion;
import com.solif.backend.domain.councilreview.entity.CouncilReviewRelay;
import com.solif.backend.domain.councilreview.repository.CouncilReviewParticipantRepository;
import com.solif.backend.domain.councilreview.repository.CouncilReviewPostRepository;
import com.solif.backend.domain.councilreview.repository.CouncilReviewRelayRepository;
import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import com.solif.backend.domain.file.repository.FileAttachmentRepository;
import com.solif.backend.domain.file.service.FileService;
import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.repository.PostRepository;
import com.solif.backend.domain.postlike.repository.PostLikeRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.domain.notification.entity.NotificationType;
import com.solif.backend.domain.notification.entity.NotificationTargetType;
import com.solif.backend.domain.notification.event.NotificationEvent;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilReviewPostService {

    private final CouncilReviewPostRepository reviewPostRepository;
    private final CouncilReviewRelayRepository relayRepository;
    private final CouncilReviewParticipantRepository participantRepository;
    private final CouncilReviewQuestionService questionService;
    private final CouncilReviewRelayService relayService;
    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final FileService fileService;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 자치회별 활동 후기 목록 조회
    public Slice<CouncilReviewPostListResponse> getCouncilReviewPosts(Long councilId, Pageable pageable) {
        log.info("활동 후기 목록 조회 - councilId: {}", councilId);

        // 자치회 존재 확인
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.COUNCIL_NOT_FOUND));

        // 활동 후기 목록 조회
        Slice<CouncilReviewPost> posts = reviewPostRepository.findByCouncilIdWithPostAndCouncil(councilId, pageable);

        // 빈 결과 처리
        if (!posts.hasContent()) {
            return posts.map(post -> CouncilReviewPostListResponse.from(
                    post, 0L, 0L, 0L, 0L, null
            ));
        }

        // 게시글 ID 목록 추출
        List<Long> councilReviewPostIds = posts.getContent().stream()
                .map(CouncilReviewPost::getCouncilReviewPostId)
                .collect(Collectors.toList());

        // Post ID 목록 추출 (좋아요, 댓글 카운트용)
        List<Long> postIds = posts.getContent().stream()
                .map(post -> post.getPost().getPostId())
                .collect(Collectors.toList());

        // 배치 쿼리로 한 번에 조회 (N+1 방지)
        Map<Long, Long> participantCounts = participantRepository.countByPostIds(councilReviewPostIds);
        Map<Long, Long> relayCounts = relayRepository.countByPostIds(councilReviewPostIds);
        Map<Long, Long> likeCounts = postLikeRepository.countByPostIds(postIds);
        Map<Long, Long> commentCounts = commentRepository.countByPostIdsAsMap(postIds);

        // N+1 해결: 대표 이미지를 배치로 조회
        Map<Long, String> thumbnailUrlMap = fileAttachmentRepository
                .findByFileTargetTypeAndFileTargetIdInAndSortOrderAndPurpose(
                        FileTargetType.COUNCIL_POST,
                        councilReviewPostIds,
                        1,
                        AttachmentPurpose.POST_ATTACHMENT
                )
                .stream()
                .collect(Collectors.toMap(
                        FileAttachment::getFileTargetId,
                        attachment -> attachment.getFile().getUrl(region),
                        (existing, replacement) -> existing  // 중복 키 처리
                ));

        // DTO 변환
        return posts.map(post -> {
            Long participantCount = participantCounts.getOrDefault(post.getCouncilReviewPostId(), 0L);
            Long relayCount = relayCounts.getOrDefault(post.getCouncilReviewPostId(), 0L);
            Long likeCount = likeCounts.getOrDefault(post.getPost().getPostId(), 0L);
            Long commentCount = commentCounts.getOrDefault(post.getPost().getPostId(), 0L);

            // 대표 이미지 조회 (Map에서 O(1) 조회)
            String thumbnailImageUrl = thumbnailUrlMap.get(post.getCouncilReviewPostId());

            return CouncilReviewPostListResponse.from(
                    post, participantCount, relayCount, likeCount, commentCount, thumbnailImageUrl
            );
        });
    }

    // 활동 후기 상세 조회
    @Transactional
    public CouncilReviewPostDetailResponse getCouncilReviewPostDetail(Long userId, Long councilReviewPostId) {
        log.info("활동 후기 상세 조회 - postId: {}, userId: {}", councilReviewPostId, userId);

        // 1. 활동 후기 조회
        CouncilReviewPost post = reviewPostRepository.findActiveByIdWithPostAndCouncil(councilReviewPostId)
                .orElseThrow(() -> new CustomException(CouncilReviewErrorCode.COUNCIL_REVIEW_POST_NOT_FOUND));

        // 2. 조회수 증가
        post.getPost().increaseViewCount();

        // 3. 이미지 URL 목록 조회
        List<FileAttachment> attachments = fileAttachmentRepository
                .findByFileTargetTypeAndFileTargetIdOrderBySortOrder(
                        FileTargetType.COUNCIL_POST,
                        councilReviewPostId
                );

        List<String> imageUrls = attachments.stream()
                .filter(attachment -> attachment.getPurpose() == AttachmentPurpose.POST_ATTACHMENT)
                .map(attachment -> attachment.getFile().getUrl(region))
                .toList();

        // 4. 참여자 목록 조회
        List<CouncilReviewParticipant> participants = participantRepository
                .findByCouncilReviewPostIdWithUser(councilReviewPostId);
        List<CouncilReviewParticipantResponse> participantResponses = participants.stream()
                .map(CouncilReviewParticipantResponse::from)
                .collect(Collectors.toList());

        // 5. 릴레이 목록 조회
        List<CouncilReviewRelayResponse> relayResponses = relayService.getRelaysByPostId(councilReviewPostId, userId);

        // 6. 좋아요 수, 댓글 수 조회
        Long likeCount = postLikeRepository.countByPost_PostId(post.getPost().getPostId());
        Long commentCount = commentRepository.countByPost_PostId(post.getPost().getPostId());

        // 7. 현재 사용자의 좋아요 여부 조회
        Boolean isLikedByMe = postLikeRepository.existsByUser_UserIdAndPost_PostId(
                userId, post.getPost().getPostId()
        );

        // 8. 현재 사용자가 리더인지 확인
        Boolean isLeader = post.isLeader(userId);

        return CouncilReviewPostDetailResponse.of(
                post, imageUrls, participantResponses, relayResponses,
                likeCount, commentCount, isLikedByMe, isLeader
        );
    }

    // 활동 후기 생성 (리더 전용)
    @Transactional
    public CouncilReviewPostCreateResponse createCouncilReviewPost(
            Long userId, Long councilId, CouncilReviewPostCreateRequest request
    ) {
        log.info("활동 후기 생성 - userId: {}, councilId: {}", userId, councilId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 2. 자치회 조회
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> new CustomException(CouncilErrorCode.COUNCIL_NOT_FOUND));

        // 3. 리더 권한 확인
        if (!council.isLeader(userId)) {
            throw new CustomException(CouncilReviewErrorCode.ONLY_LEADER_CAN_CREATE_REVIEW);
        }

        // 4. 참여자 검증 (모두 자치회 멤버여야 함)
        validateParticipants(councilId, request.getParticipantUserIds());

        // 5. Board 조회 (boardId = 1: 자치회 활동 후기)
        Board board = boardRepository.findById(1L)
                .orElseThrow(() -> new CustomException(BoardErrorCode.BOARD_NOT_FOUND));

        // 6. Post 생성 (통합 게시글)
        Post post = Post.builder()
                .author(user)
                .board(board)
                .postCategory(null) // 자치회 활동 후기는 카테고리 없음
                .postTitle(request.getPostTitle())
                .postContent(null) // 활동 후기는 릴레이로 내용 작성
                .build();
        Post savedPost = postRepository.save(post);

        // 7. CouncilReviewPost 생성
        CouncilReviewPost reviewPost = CouncilReviewPost.builder()
                .post(savedPost)
                .council(council)
                .activityDate(request.getActivityDate())
                .activityLocation(request.getActivityLocation())
                .totalCost(request.getTotalCost())
                .build();
        CouncilReviewPost savedReviewPost = reviewPostRepository.save(reviewPost);

        // 8. 참여자 생성
        createParticipants(savedReviewPost, request.getParticipantUserIds());

        // 9. 질문 검증
        CouncilReviewQuestion question = questionService.validateAndGetQuestion(request.getQuestionId());

        // 10. 리더의 첫 번째 릴레이 생성 (relay_order = 1)
        CouncilReviewRelay leaderRelay = CouncilReviewRelay.builder()
                .councilReviewPost(savedReviewPost)
                .councilReviewQuestion(question)
                .writerUser(user)
                .relayOrder(1)
                .relayContent(request.getRelayContent())
                .build();
        relayRepository.save(leaderRelay);

        // 11. 예산 차감
        council.decreaseBudget(request.getTotalCost());

        // 12. 이미지 첨부
        // 파일 확정 (일반 이미지)
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            fileService.confirmFiles(
                    request.getFileIds(),
                    FileTargetType.COUNCIL_POST,
                    savedReviewPost.getCouncilReviewPostId(),
                    AttachmentPurpose.POST_ATTACHMENT
            );
        }

        // 파일 확정 (영수증)
        if (request.getReceiptFileId() != null) {
            fileService.confirmFiles(
                    List.of(request.getReceiptFileId()),
                    FileTargetType.COUNCIL_POST,
                    savedReviewPost.getCouncilReviewPostId(),
                    AttachmentPurpose.RECEIPT
            );
        }

        log.info("활동 후기 생성 완료 - reviewPostId: {}, postId: {}",
                savedReviewPost.getCouncilReviewPostId(), savedPost.getPostId());

        // 알림 발송: 팀원 전체에게 활동 시작 알림 (작성자 제외)
        List<Long> memberUserIds = councilMemberRepository.findUserIdsByCouncilId(councilId);
        for (Long memberId : memberUserIds) {
            if (!memberId.equals(userId)) {
                eventPublisher.publishEvent(new NotificationEvent(
                        memberId,
                        NotificationType.COUNCIL_ACTIVITY_START,
                        NotificationTargetType.COUNCIL_POST,
                        savedReviewPost.getCouncilReviewPostId(),
                        "릴레이 활동이 시작됐어요!",
                        "릴레이 활동이 시작됐어요! 내 파트를 작성해 주세요."
                ));
            }
        }

        return CouncilReviewPostCreateResponse.of(
                savedReviewPost.getCouncilReviewPostId(),
                savedPost.getPostId()
        );
    }

    // 활동 후기 수정 (리더 전용)
    @Transactional
    public void updateCouncilReviewPost(Long userId, Long councilReviewPostId, CouncilReviewPostUpdateRequest request) {
        log.info("활동 후기 수정 - userId: {}, postId: {}", userId, councilReviewPostId);

        // 1. 활동 후기 조회
        CouncilReviewPost reviewPost = reviewPostRepository.findByIdWithPostAndCouncil(councilReviewPostId)
                .orElseThrow(() -> new CustomException(CouncilReviewErrorCode.COUNCIL_REVIEW_POST_NOT_FOUND));

        // 2. 삭제된 게시글 체크
        if (reviewPost.isDeleted()) {
            throw new CustomException(CouncilReviewErrorCode.COUNCIL_REVIEW_POST_DELETED);
        }

        // 3. 리더 권한 확인
        if (!reviewPost.isLeader(userId)) {
            throw new CustomException(CouncilReviewErrorCode.ONLY_LEADER_CAN_UPDATE_REVIEW);
        }

        // 4. 참여자 검증
        validateParticipants(reviewPost.getCouncil().getCouncilId(), request.getParticipantUserIds());

        // 5. 예산 재계산 (원래 금액 복구 후 재차감)
        Long oldTotalCost = reviewPost.getOldTotalCost();
        Long newTotalCost = request.getTotalCost();

        Council council = reviewPost.getCouncil();
        council.increaseBudget(oldTotalCost);    // 복구
        council.decreaseBudget(newTotalCost);    // 재차감

        // 6. 활동 후기 정보 업데이트
        reviewPost.updateReviewPost(
                request.getPostTitle(),
                request.getActivityDate(),
                request.getActivityLocation(),
                request.getTotalCost()
        );

        // 7. 참여자 업데이트
        updateParticipants(reviewPost, request.getParticipantUserIds());

        // 8. 일반 이미지 파일 처리
        if (request.getFileIds() != null) {
            // 기존 일반 이미지 조회
            List<FileAttachment> existingImages = fileAttachmentRepository
                    .findByFileTargetTypeAndFileTargetId(
                            FileTargetType.COUNCIL_POST,
                            councilReviewPostId
                    )
                    .stream()
                    .filter(attachment -> attachment.getPurpose() == AttachmentPurpose.POST_ATTACHMENT)
                    .toList();

            // 기존 파일 ID 목록
            List<Long> existingFileIds = existingImages.stream()
                    .map(attachment -> attachment.getFile().getFileId())
                    .toList();

            // 삭제할 파일 ID (기존에는 있지만 요청에는 없는 파일)
            List<Long> fileIdsToDelete = existingFileIds.stream()
                    .filter(fileId -> !request.getFileIds().contains(fileId))
                    .toList();

            // 새로 추가할 파일 ID (요청에는 있지만 기존에는 없는 TEMP 파일)
            List<Long> fileIdsToAdd = request.getFileIds().stream()
                    .filter(fileId -> !existingFileIds.contains(fileId))
                    .toList();

            // 삭제 대상 파일 완전 삭제 (File + S3)
            for (FileAttachment attachment : existingImages) {
                if (fileIdsToDelete.contains(attachment.getFile().getFileId())) {
                    fileService.detachFile(attachment.getFileAttachmentId());
                }
            }

            // 새 파일만 확정 (TEMP → PERMANENT)
            if (!fileIdsToAdd.isEmpty()) {
                fileService.confirmFiles(
                        fileIdsToAdd,
                        FileTargetType.COUNCIL_POST,
                        councilReviewPostId,
                        AttachmentPurpose.POST_ATTACHMENT
                );
            }

            // 유지되는 PERMANENT 파일은 Attachment 그대로 유지
        }

        // 9. 영수증 파일 처리
        if (request.getReceiptFileId() != null) {
            // 기존 영수증 삭제
            List<FileAttachment> existingReceipts = fileAttachmentRepository
                    .findByFileTargetTypeAndFileTargetId(
                            FileTargetType.COUNCIL_POST,
                            councilReviewPostId
                    )
                    .stream()
                    .filter(attachment -> attachment.getPurpose() == AttachmentPurpose.RECEIPT)
                    .toList();

            for (FileAttachment attachment : existingReceipts) {
                fileService.detachFile(attachment.getFileAttachmentId());
            }

            // 새 영수증 확정
            fileService.confirmFiles(
                    List.of(request.getReceiptFileId()),
                    FileTargetType.COUNCIL_POST,
                    councilReviewPostId,
                    AttachmentPurpose.RECEIPT
            );
        }

        log.info("활동 후기 수정 완료 - postId: {}", councilReviewPostId);
    }

    // 활동 후기 삭제 (리더 전용, Soft Delete)
    @Transactional
    public void deleteCouncilReviewPost(Long userId, Long councilReviewPostId) {
        log.info("활동 후기 삭제 - userId: {}, postId: {}", userId, councilReviewPostId);

        // 1. 활동 후기 조회
        CouncilReviewPost reviewPost = reviewPostRepository.findByIdWithPostAndCouncil(councilReviewPostId)
                .orElseThrow(() -> new CustomException(CouncilReviewErrorCode.COUNCIL_REVIEW_POST_NOT_FOUND));

        // 2. 이미 삭제된 게시글 체크
        if (reviewPost.isDeleted()) {
            throw new CustomException(CouncilReviewErrorCode.COUNCIL_REVIEW_POST_DELETED);
        }

        // 3. 리더 권한 확인
        if (!reviewPost.isLeader(userId)) {
            throw new CustomException(CouncilReviewErrorCode.ONLY_LEADER_CAN_DELETE_REVIEW);
        }

        // 4. Soft Delete (council_review_post, post)
        reviewPost.softDelete();

        // 5. 모든 릴레이 Hard Delete
        relayRepository.deleteByCouncilReviewPostId(councilReviewPostId);

        // 6. 모든 참여자 Hard Delete
        participantRepository.deleteByCouncilReviewPostId(councilReviewPostId);

        // 7. 예산 복구
        Council council = reviewPost.getCouncil();
        council.increaseBudget(reviewPost.getTotalCost());

        log.info("활동 후기 삭제 완료 - postId: {}", councilReviewPostId);
    }

    // 참여자 검증 (모두 자치회 멤버여야 함)
    private void validateParticipants(Long councilId, List<Long> participantUserIds) {
        log.info("참여자 검증 - councilId: {}, participantUserIds: {}", councilId, participantUserIds);

        if (participantUserIds == null || participantUserIds.isEmpty()) {
            throw new CustomException(CouncilReviewErrorCode.PARTICIPANT_LIST_EMPTY);
        }

        // 자치회 멤버 목록 조회
        List<CouncilMember> councilMembers = councilMemberRepository.findByCouncil_CouncilId(councilId);
        Set<Long> memberUserIds = councilMembers.stream()
                .map(member -> member.getUser().getUserId())
                .collect(Collectors.toSet());

        // 참여자가 모두 자치회 멤버인지 확인
        for (Long participantUserId : participantUserIds) {
            if (!memberUserIds.contains(participantUserId)) {
                log.warn("자치회 멤버가 아닌 사용자 발견 - userId: {}", participantUserId);
                throw new CustomException(CouncilReviewErrorCode.PARTICIPANT_NOT_COUNCIL_MEMBER);
            }
        }

        log.info("참여자 검증 완료 - 모두 자치회 멤버");
    }

    // 참여자 생성
    private void createParticipants(CouncilReviewPost reviewPost, List<Long> participantUserIds) {
        log.info("참여자 생성 - reviewPostId: {}, count: {}",
                reviewPost.getCouncilReviewPostId(), participantUserIds.size());

        List<CouncilReviewParticipant> participants = new ArrayList<>();

        for (Long userId : participantUserIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

            CouncilReviewParticipant participant = CouncilReviewParticipant.builder()
                    .councilReviewPost(reviewPost)
                    .user(user)
                    .build();
            participants.add(participant);
        }

        participantRepository.saveAll(participants);
        log.info("참여자 생성 완료 - {} 명", participants.size());
    }

    // 참여자 업데이트 (추가/제거)
    private void updateParticipants(CouncilReviewPost reviewPost, List<Long> newParticipantUserIds) {
        log.info("참여자 업데이트 - reviewPostId: {}", reviewPost.getCouncilReviewPostId());

        Long postId = reviewPost.getCouncilReviewPostId();

        // 기존 참여자 목록
        List<Long> oldParticipantUserIds = participantRepository.findUserIdsByCouncilReviewPostId(postId);

        // 추가할 참여자 (new - old)
        Set<Long> toAdd = new HashSet<>(newParticipantUserIds);
        toAdd.removeAll(oldParticipantUserIds);

        // 제거할 참여자 (old - new)
        Set<Long> toRemove = new HashSet<>(oldParticipantUserIds);
        toRemove.removeAll(newParticipantUserIds);

        // 참여자 추가
        for (Long userId : toAdd) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

            CouncilReviewParticipant participant = CouncilReviewParticipant.builder()
                    .councilReviewPost(reviewPost)
                    .user(user)
                    .build();
            participantRepository.save(participant);
        }

        // 참여자 제거 (해당 멤버의 릴레이도 함께 삭제)
        for (Long userId : toRemove) {
            participantRepository.deleteByCouncilReviewPostIdAndUserId(postId, userId);
            relayRepository.deleteByCouncilReviewPostIdAndWriterUserId(postId, userId);
        }

        // 릴레이 순서 재정렬 (제거된 릴레이가 있으면)
        if (!toRemove.isEmpty()) {
            List<CouncilReviewRelay> relays = relayRepository
                    .findByCouncilReviewPostIdOrderByRelayOrder(postId);
            for (int i = 0; i < relays.size(); i++) {
                relays.get(i).updateRelayOrder(i + 1);
            }
            relayRepository.saveAll(relays);
        }

        log.info("참여자 업데이트 완료 - 추가: {} 명, 제거: {} 명", toAdd.size(), toRemove.size());
    }
}