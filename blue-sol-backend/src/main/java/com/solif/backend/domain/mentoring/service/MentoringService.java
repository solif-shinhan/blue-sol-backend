package com.solif.backend.domain.mentoring.service;

import com.solif.backend.domain.auth.code.AuthErrorCode;
import com.solif.backend.domain.file.entity.AttachmentPurpose;
import com.solif.backend.domain.file.entity.FileAttachment;
import com.solif.backend.domain.file.entity.FileTargetType;
import com.solif.backend.domain.file.repository.FileAttachmentRepository;
import com.solif.backend.domain.file.service.FileService;
import com.solif.backend.domain.interest.entity.UserInterest;
import com.solif.backend.domain.interest.repository.UserInterestRepository;
import com.solif.backend.domain.mentoring.code.MentoringErrorCode;
import com.solif.backend.domain.mentoring.dto.*;
import com.solif.backend.domain.mentoring.entity.*;
import com.solif.backend.domain.mentoring.repository.MentorRepository;
import com.solif.backend.domain.mentoring.repository.MentoringCardRepository;
import com.solif.backend.domain.mentoring.repository.MentoringInteractionRepository;
import com.solif.backend.domain.mentoring.repository.MentoringRequestRepository;
import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.repository.PostRepository;
import com.solif.backend.domain.profile.entity.UserProfile;
import com.solif.backend.domain.profile.repository.UserProfileRepository;
import com.solif.backend.domain.user.entity.User;
import com.solif.backend.domain.user.repository.UserRepository;
import com.solif.backend.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
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
public class MentoringService {

    private final MentorRepository mentorRepository;
    private final MentoringRequestRepository mentoringRequestRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserInterestRepository userInterestRepository;
    private final PostRepository postRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final MentoringCardRepository mentoringCardRepository;
    private final FileService fileService;
    private final MentoringInteractionRepository mentoringInteractionRepository;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 전문가 멘토 목록 조회
    public List<MentorListResponse> getMentors() {
        log.info("전문가 멘토 목록 조회");

        List<Mentor> mentors = mentorRepository.findByIsActiveTrueOrderByCreatedAtDesc();

        // N+1 방지: 배치로 프로필 이미지 조회
        List<Long> mentorIds = mentors.stream()
                .map(Mentor::getMentorId)
                .collect(Collectors.toList());

        // 기존 메서드 사용: findByFileTargetTypeAndTargetIdInAndSortOrderAndPurpose
        Map<Long, String> profileImageMap = mentorIds.isEmpty() ? Map.of() :
                fileAttachmentRepository.findByFileTargetTypeAndTargetIdInAndSortOrderAndPurpose(
                                FileTargetType.MENTOR_PROFILE,
                                mentorIds,
                                1,
                                AttachmentPurpose.PROFILE_IMAGE
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                FileAttachment::getTargetId,
                                attachment -> attachment.getFile().getUrl(region),
                                (existing, replacement) -> existing
                        ));

        return mentors.stream()
                .map(mentor -> MentorListResponse.from(
                        mentor,
                        profileImageMap.get(mentor.getMentorId())
                ))
                .collect(Collectors.toList());
    }

    // 멘토링 신청
    @Transactional
    public MentoringRequestCreateResponse createMentoringRequest(Long userId, MentoringRequestCreateRequest request) {
        log.info("멘토링 신청 - userId: {}, mentorId: {}", userId, request.getMentorId());

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 멘토 조회
        Mentor mentor = mentorRepository.findById(request.getMentorId())
                .orElseThrow(() -> new CustomException(MentoringErrorCode.MENTOR_NOT_FOUND));

        // Enum 변환
        MentoringCategory category = request.getCategory();
        MentoringMethod method = request.getMethod();

        // 멘토링 신청 생성
        MentoringRequest mentoringRequest = MentoringRequest.builder()
                .mentor(mentor)
                .menteeUser(user)
                .category(category)
                .content(request.getContent())
                .method(method)
                .build();

        MentoringRequest saved = mentoringRequestRepository.save(mentoringRequest);

        return MentoringRequestCreateResponse.from(saved);
    }

    // 내가 보낸 신청서 목록 조회
    public Slice<MentoringRequestListResponse> getSentRequests(Long userId, Pageable pageable) {
        log.info("내가 보낸 신청서 목록 조회 - userId: {}", userId);

        Slice<MentoringRequest> requests = mentoringRequestRepository.findSentRequests(userId, pageable);

        return requests.map(MentoringRequestListResponse::from);
    }

    // 멘토링 신청서 상세 조회
    public MentoringRequestDetailResponse getRequestDetail(Long userId, Long requestId) {
        log.info("멘토링 신청서 상세 조회 - userId: {}, requestId: {}", userId, requestId);

        // 신청서 조회
        MentoringRequest request = mentoringRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new CustomException(MentoringErrorCode.MENTORING_REQUEST_NOT_FOUND));

        // 권한 확인 (신청자 본인만 조회 가능)
        if (!request.isMentee(userId)) {
            throw new CustomException(MentoringErrorCode.UNAUTHORIZED_REQUEST_ACCESS);
        }

        // 멘토 프로필 이미지 조회
        String mentorProfileImageUrl = null;
        Optional<FileAttachment> profileImage = fileAttachmentRepository
                .findByFileTargetTypeAndTargetIdAndSortOrderAndPurpose(
                        FileTargetType.MENTOR_PROFILE,
                        request.getMentor().getMentorId(),
                        1,
                        AttachmentPurpose.PROFILE_IMAGE
                );

        if (profileImage.isPresent()) {
            mentorProfileImageUrl = profileImage.get().getFile().getUrl(region);
        }

        return MentoringRequestDetailResponse.from(request, mentorProfileImageUrl);
    }

    // 내가 받은 답변 목록 조회
    public Slice<MentoringRequestListResponse> getReceivedRequests(Long userId, Pageable pageable) {
        log.info("받은 신청서 목록 조회 - userId: {}", userId);

        Slice<MentoringRequest> requests = mentoringRequestRepository.findReceivedRequests(userId, pageable);

        return requests.map(MentoringRequestListResponse::from);
    }

    // 멘토링 홈 조회
    public MentoringHomeResponse getMentoringHome(Long userId) {
        log.info("멘토링 홈 조회 - userId: {}", userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 1. 전문가 멘토 목록 조회 (카테고리별)
        List<Mentor> allMentors = mentorRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        List<Mentor> studyMentors = mentorRepository.findByIsActiveTrueAndMentorCategoryOrderByCreatedAtDesc(MentorCategory.STUDY);
        List<Mentor> jobMentors = mentorRepository.findByIsActiveTrueAndMentorCategoryOrderByCreatedAtDesc(MentorCategory.JOB);
        List<Mentor> lifeMentors = mentorRepository.findByIsActiveTrueAndMentorCategoryOrderByCreatedAtDesc(MentorCategory.LIFE);

        // 프로필 이미지 배치 조회
        List<Long> allMentorIds = allMentors.stream()
                .map(Mentor::getMentorId)
                .collect(Collectors.toList());

        Map<Long, String> profileImageMap = allMentorIds.isEmpty() ? Map.of() :
                fileAttachmentRepository.findByFileTargetTypeAndTargetIdInAndSortOrderAndPurpose(
                                FileTargetType.MENTOR_PROFILE,
                                allMentorIds,
                                1,
                                AttachmentPurpose.PROFILE_IMAGE
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                FileAttachment::getTargetId,
                                attachment -> attachment.getFile().getUrl(region),
                                (existing, replacement) -> existing
                        ));

        // DTO 변환
        List<MentorListResponse> allMentorResponses = allMentors.stream()
                .map(mentor -> MentorListResponse.from(mentor, profileImageMap.get(mentor.getMentorId())))
                .collect(Collectors.toList());

        List<MentorListResponse> studyMentorResponses = studyMentors.stream()
                .map(mentor -> MentorListResponse.from(mentor, profileImageMap.get(mentor.getMentorId())))
                .collect(Collectors.toList());

        List<MentorListResponse> jobMentorResponses = jobMentors.stream()
                .map(mentor -> MentorListResponse.from(mentor, profileImageMap.get(mentor.getMentorId())))
                .collect(Collectors.toList());

        List<MentorListResponse> lifeMentorResponses = lifeMentors.stream()
                .map(mentor -> MentorListResponse.from(mentor, profileImageMap.get(mentor.getMentorId())))
                .collect(Collectors.toList());

        // 2. 선후배 멘토링 - 응원하기 (내가 선배에게 응원)
        MentoringHomeResponse.SeniorJuniorMentoringResponse cheerList =
                buildCheerList(currentUser);

        // 3. 선후배 멘토링 - 경험나누기 (내가 후배에게 경험 나누기)
        MentoringHomeResponse.SeniorJuniorMentoringResponse helpList =
                buildHelpList(currentUser);

        // 4. 멘토링 후기 목록 (boardId=2, 최대 4개)
        Pageable topReviews = PageRequest.of(0, 4);
        Slice<Post> reviewPosts = postRepository.findByBoard_BoardIdAndDeletedAtIsNullWithAuthor(2L, topReviews);

        List<MentoringHomeResponse.MentoringReviewResponse> reviewResponses = reviewPosts.stream()
                .map(post -> MentoringHomeResponse.MentoringReviewResponse.builder()
                        .postId(post.getPostId())
                        .title(post.getPostTitle())
                        .authorName(post.getAuthor().getName())
                        .category(post.getPostCategory() != null ? post.getPostCategory().name() : null)
                        .viewCount(post.getViewCount())
                        .build())
                .collect(Collectors.toList());

        return MentoringHomeResponse.builder()
                .allMentors(allMentorResponses)
                .studyMentors(studyMentorResponses)
                .jobMentors(jobMentorResponses)
                .lifeMentors(lifeMentorResponses)
                .cheerList(cheerList)
                .helpList(helpList)
                .reviews(reviewResponses)
                .build();
    }

    // 멘토링 엽서 발송
    @Transactional
    public MentoringCardSendResponse sendMentoringCard(Long userId, MentoringCardSendRequest request) {
        log.info("멘토링 엽서 발송 - userId: {}", userId);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // Enum 변환
        MentoringCategory category = request.getCategory();
        MentoringMethod method = request.getMethod();

        // 멘토링 엽서 생성
        MentoringCard card = MentoringCard.builder()
                .sender(user)
                .cardTitle(request.getCardTitle())
                .category(category)
                .method(method)
                .cardContent(request.getCardContent())
                .build();

        MentoringCard savedCard = mentoringCardRepository.save(card);

        // 파일 확정 (fileIds가 있으면)
        if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
            fileService.confirmFiles(
                    request.getFileIds(),
                    FileTargetType.MENTORING_CARD,
                    savedCard.getMentoringCardId(),
                    AttachmentPurpose.MENTORING_CARD_ATTACHMENT
            );
        }

        return MentoringCardSendResponse.from(savedCard);
    }

    // 보낸 멘토링 엽서 목록 조회
    public Slice<MentoringCardListResponse> getSentCards(Long userId, Pageable pageable) {
        log.info("보낸 멘토링 엽서 목록 조회 - userId: {}", userId);

        Slice<MentoringCard> cards = mentoringCardRepository.findSentCards(userId, pageable);

        return cards.map(MentoringCardListResponse::from);
    }

    // 멘토링 엽서 상세 조회
    @Transactional
    public MentoringCardDetailResponse getCardDetail(Long userId, Long cardId) {
        log.info("멘토링 엽서 상세 조회 - userId: {}, cardId: {}", userId, cardId);

        MentoringCard card = mentoringCardRepository.findById(cardId)
                .orElseThrow(() -> new CustomException(MentoringErrorCode.MENTORING_CARD_NOT_FOUND));

        // 권한 확인 (본인만 조회 가능)
        if (!card.isSender(userId)) {
            throw new CustomException(MentoringErrorCode.UNAUTHORIZED_CARD_ACCESS);
        }

        // 읽음 처리
        if (!card.getIsRead()) {
            card.markAsRead();
        }

        // 첨부 파일 URL 조회
        List<FileAttachment> attachments = fileAttachmentRepository
                .findByFileTargetTypeAndTargetIdOrderBySortOrder(FileTargetType.MENTORING_CARD, cardId);

        List<String> imageUrls = attachments.stream()
                .map(attachment -> attachment.getFile().getUrl(region))
                .toList();

        return MentoringCardDetailResponse.from(card, imageUrls);
    }

    // === Helper Methods ===

    // 응원하기 리스트 생성
    private MentoringHomeResponse.SeniorJuniorMentoringResponse buildCheerList(User currentUser) {
        // MASTER(관리자)는 응원하기 대상 없음
        if (currentUser.getUserRole() == User.UserRole.MASTER) {
            return MentoringHomeResponse.SeniorJuniorMentoringResponse.builder()
                    .users(List.of())
                    .build();
        }

        List<MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse> userCards = new ArrayList<>();

        // 1) 내가 받은 응원 중 아직 내가 보내지 않은 사람들 (대기중)
        List<MentoringInteraction> pendingInteractions = mentoringInteractionRepository
                .findPendingReceivedInteractions(currentUser.getUserId(), MentoringInteractionType.CHEER);

        for (MentoringInteraction interaction : pendingInteractions) {
            User sender = interaction.getSender();
            userCards.add(buildUserCard(sender, "PENDING"));
        }

        // 2) 양방향 완료된 응원 (서로 보낸 사람들)
        List<MentoringInteraction> completedInteractions = mentoringInteractionRepository
                .findCompletedInteractions(currentUser.getUserId(), MentoringInteractionType.CHEER);

        // 중복 제거를 위해 Set 사용
        Set<Long> addedUserIds = userCards.stream()
                .map(MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse::getUserId)
                .collect(Collectors.toSet());

        for (MentoringInteraction interaction : completedInteractions) {
            User otherUser = interaction.getSender().getUserId().equals(currentUser.getUserId())
                    ? interaction.getReceiver()
                    : interaction.getSender();

            if (!addedUserIds.contains(otherUser.getUserId())) {
                userCards.add(buildUserCard(otherUser, "COMPLETED"));
                addedUserIds.add(otherUser.getUserId());
            }
        }

        // 최대 10명으로 제한
        List<MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse> limitedCards =
                userCards.stream().limit(10).collect(Collectors.toList());

        return MentoringHomeResponse.SeniorJuniorMentoringResponse.builder()
                .users(limitedCards)
                .build();
    }

    // 경험나누기 리스트 생성
    private MentoringHomeResponse.SeniorJuniorMentoringResponse buildHelpList(User currentUser) {
        // MASTER(관리자)는 경험나누기 대상 없음
        if (currentUser.getUserRole() == User.UserRole.MASTER) {
            return MentoringHomeResponse.SeniorJuniorMentoringResponse.builder()
                    .users(List.of())
                    .build();
        }

        List<MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse> userCards = new ArrayList<>();

        // 1) 내가 받은 경험나누기 중 아직 내가 보내지 않은 사람들 (대기중)
        List<MentoringInteraction> pendingInteractions = mentoringInteractionRepository
                .findPendingReceivedInteractions(currentUser.getUserId(), MentoringInteractionType.HELP);

        for (MentoringInteraction interaction : pendingInteractions) {
            User sender = interaction.getSender();
            userCards.add(buildUserCard(sender, "PENDING"));
        }

        // 2) 양방향 완료된 경험나누기 (서로 보낸 사람들)
        List<MentoringInteraction> completedInteractions = mentoringInteractionRepository
                .findCompletedInteractions(currentUser.getUserId(), MentoringInteractionType.HELP);

        // 중복 제거를 위해 Set 사용
        Set<Long> addedUserIds = userCards.stream()
                .map(MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse::getUserId)
                .collect(Collectors.toSet());

        for (MentoringInteraction interaction : completedInteractions) {
            User otherUser = interaction.getSender().getUserId().equals(currentUser.getUserId())
                    ? interaction.getReceiver()
                    : interaction.getSender();

            if (!addedUserIds.contains(otherUser.getUserId())) {
                userCards.add(buildUserCard(otherUser, "COMPLETED"));
                addedUserIds.add(otherUser.getUserId());
            }
        }

        // 최대 10명으로 제한
        List<MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse> limitedCards =
                userCards.stream().limit(10).collect(Collectors.toList());

        return MentoringHomeResponse.SeniorJuniorMentoringResponse.builder()
                .users(limitedCards)
                .build();
    }

    // UserCard 생성 헬퍼 메서드
    private MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse buildUserCard(User user, String status) {
        UserProfile profile = userProfileRepository.findByUser_UserId(user.getUserId()).orElse(null);
        List<String> interests = userInterestRepository.findAllByUser_UserId(user.getUserId()).stream()
                .map(UserInterest::getCategoryName)
                .collect(Collectors.toList());

        return MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse.builder()
                .userId(user.getUserId())
                .userName(user.getName())
                .character(profile != null ? profile.getUserCharacter() : null)
                .backgroundPattern(profile != null ? profile.getBackgroundPattern() : null)
                .solidGoalName(profile != null ? profile.getSolidGoalName() : null)
                .interests(interests)
                .status(status) // "PENDING" 또는 "COMPLETED"
                .build();
    }
}