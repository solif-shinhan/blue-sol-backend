package com.solif.backend.domain.mission.entity;

public enum MissionConditionType {
    PROFILE_VIEW,           // 프로필 조회
    CONNECTION_ACTION,      // 응원/경험나누기
    MESSAGE,                // 쪽지 발송
    PROFILE_COMPLETE,       // 프로필 완성
    MESSAGE_THREAD,         // 경험나누기 → 쪽지 대화
    POST_VIEW,              // 재단 소식 조회
    POST_CREATE,            // 활동 게시글 작성
    COMMENT_CREATE,         // 댓글 작성
    MENTORING_COMPLETE      // 멘토링 완료
}