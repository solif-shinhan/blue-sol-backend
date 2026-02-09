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

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        MentoringCategory category;
        MentoringMethod method;

        try {
            category = MentoringCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(MentoringErrorCode.INVALID_CATEGORY);
        }

        try {
            method = MentoringMethod.valueOf(request.getMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(MentoringErrorCode.INVALID_METHOD);
        }

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

        // 1. 전문가 멘토 목록 (최대 4개)
        List<Mentor> mentors = mentorRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        List<Mentor> topMentors = mentors.stream().limit(4).collect(Collectors.toList());

        List<Long> mentorIds = topMentors.stream()
                .map(Mentor::getMentorId)
                .collect(Collectors.toList());

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

        List<MentorListResponse> mentorResponses = topMentors.stream()
                .map(mentor -> MentorListResponse.from(mentor, profileImageMap.get(mentor.getMentorId())))
                .collect(Collectors.toList());

        // 2. 선후배 멘토링 - 응원하기 (나보다 선배)
        List<User> seniors = getSeniors(currentUser);
        MentoringHomeResponse.SeniorJuniorMentoringResponse cheerList =
                buildSeniorJuniorResponse(seniors);

        // 3. 선후배 멘토링 - 경험나누기 (나와 같거나 후배)
        // TODO: 선후배 멘토링 리스트 표시 추가 조건 구현 필요
        // 팀원(장난영)과 논의 후 적용
        List<User> juniorsAndSame = getJuniorsAndSame(currentUser);
        MentoringHomeResponse.SeniorJuniorMentoringResponse helpList =
                buildSeniorJuniorResponse(juniorsAndSame);

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
                .mentors(mentorResponses)
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
        MentoringCategory category;
        MentoringMethod method;

        try {
            category = MentoringCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(MentoringErrorCode.INVALID_CATEGORY);
        }

        try {
            method = MentoringMethod.valueOf(request.getMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(MentoringErrorCode.INVALID_METHOD);
        }

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
                    AttachmentPurpose.POST_ATTACHMENT
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

        // 첨부 파일 URL 조회
        List<FileAttachment> attachments = fileAttachmentRepository
                .findByFileTargetTypeAndTargetIdOrderBySortOrder(FileTargetType.MENTORING_CARD, cardId);

        List<String> fileUrls = attachments.stream()
                .map(attachment -> attachment.getFile().getUrl(region))
                .toList();

        return MentoringCardDetailResponse.from(card, fileUrls);
    }

    // === Helper Methods ===

    private List<User> getSeniors(User currentUser) {
        User.UserRole currentRole = currentUser.getUserRole();

        // 나보다 높은 레벨 찾기
        return switch (currentRole) {
            case JUNIOR -> userRepository.findAll().stream()
                    .filter(u -> u.getUserRole() == User.UserRole.SENIOR ||
                            u.getUserRole() == User.UserRole.GRADUATE ||
                            u.getUserRole() == User.UserRole.MASTER)
                    .collect(Collectors.toList());
            case SENIOR -> userRepository.findAll().stream()
                    .filter(u -> u.getUserRole() == User.UserRole.GRADUATE ||
                            u.getUserRole() == User.UserRole.MASTER)
                    .collect(Collectors.toList());
            case GRADUATE -> userRepository.findAll().stream()
                    .filter(u -> u.getUserRole() == User.UserRole.MASTER)
                    .collect(Collectors.toList());
            case MASTER -> List.of(); // 최고 레벨
        };
    }

    private List<User> getJuniorsAndSame(User currentUser) {
        User.UserRole currentRole = currentUser.getUserRole();

        // 나와 같거나 낮은 레벨 찾기
        return switch (currentRole) {
            case JUNIOR -> userRepository.findAll().stream()
                    .filter(u -> u.getUserRole() == User.UserRole.JUNIOR)
                    .filter(u -> !u.getUserId().equals(currentUser.getUserId())) // 본인 제외
                    .collect(Collectors.toList());
            case SENIOR -> userRepository.findAll().stream()
                    .filter(u -> u.getUserRole() == User.UserRole.JUNIOR ||
                            u.getUserRole() == User.UserRole.SENIOR)
                    .filter(u -> !u.getUserId().equals(currentUser.getUserId()))
                    .collect(Collectors.toList());
            case GRADUATE -> userRepository.findAll().stream()
                    .filter(u -> u.getUserRole() == User.UserRole.JUNIOR ||
                            u.getUserRole() == User.UserRole.SENIOR ||
                            u.getUserRole() == User.UserRole.GRADUATE)
                    .filter(u -> !u.getUserId().equals(currentUser.getUserId()))
                    .collect(Collectors.toList());
            case MASTER -> userRepository.findAll().stream()
                    .filter(u -> !u.getUserId().equals(currentUser.getUserId()))
                    .collect(Collectors.toList());
        };
    }

    private MentoringHomeResponse.SeniorJuniorMentoringResponse buildSeniorJuniorResponse(List<User> users) {
        List<MentoringHomeResponse.SeniorJuniorMentoringResponse.UserCardResponse> userCards = users.stream()
                .limit(10) // 최대 10명
                .map(user -> {
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
                            .build();
                })
                .collect(Collectors.toList());

        return MentoringHomeResponse.SeniorJuniorMentoringResponse.builder()
                .users(userCards)
                .build();
    }
}