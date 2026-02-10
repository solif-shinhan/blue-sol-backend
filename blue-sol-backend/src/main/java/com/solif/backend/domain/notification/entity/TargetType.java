package com.solif.backend.domain.notification.entity;

public enum TargetType {
    POST,               // 게시글
    MESSAGE,            // 쪽지
    NETWORK,            // 교류망
    COUNCIL,            // 자치회 (홈, 규칙 등)
    COUNCIL_POST,       // 자치회 활동 후기 글
    COUNCIL_MEMBER,     // 자치회 멤버 목록
    MENTORING_REQUEST,  // 멘토링 신청
    MENTORING_CARD,     // 멘토링 엽서
    MENTORING_REVIEW,   // 멘토링 후기
    NOTICE_POST,        // 공지사항 상세
    USER_PROFILE        // SOLID 명함
}
