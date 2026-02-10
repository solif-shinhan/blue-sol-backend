package com.solif.backend.domain.notification.entity;

public enum NotificationType {
    // 게시판
    COMMENT,                // 내 글 댓글

    // 쪽지
    MESSAGE,                // 쪽지

    // 일반/기타
    CHEER,                  // 응원하기
    HELP,                   // 경험 나누기 요청
    CONNECTION,             // 교류망 추가

    // 공지
    NOTICE,                 // 공지사항

    // 자치회 활동
    COUNCIL_INVITE,         // 자치회 초대
    COUNCIL_ACTIVITY_START, // 활동 시작 (릴레이)
    COUNCIL_ACTIVITY_DONE,  // 활동 완료
    COUNCIL_COMMENT,        // 자치회 글 댓글
    COUNCIL_RULE_CHANGE,    // 규칙 변경
    COUNCIL_HOME_EDIT,      // 홈 편집
    COUNCIL_MEMBER_ADD,     // 멤버 추가
    COUNCIL_MEMBER_REMOVE,  // 강퇴/삭제

    // 멘토링
    MENTORING_REVIEW_COMMENT,   // 멘토링 후기 댓글
    MENTORING_REQUEST_RESULT,   // 멘토링 신청 결과
    MENTORING_CARD_REPLY,       // 엽서 답장
    MENTORING_REQUEST_ARRIVED   // 멘토링 요청 도착
}
