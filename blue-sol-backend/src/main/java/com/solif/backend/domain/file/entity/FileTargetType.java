package com.solif.backend.domain.file.entity;

public enum FileTargetType {
    POST,              // 통합 게시글
    COUNCIL_POST,      // 자치회 활동 후기
    PINECONE_MEMORY,
    MESSAGE,           // 쪽지 첨부파일
    MENTORING_CARD     // 멘토링 엽서 첨부파일
}
