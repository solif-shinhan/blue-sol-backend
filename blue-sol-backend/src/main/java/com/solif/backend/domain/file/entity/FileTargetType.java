package com.solif.backend.domain.file.entity;

public enum FileTargetType {
    POST,              // 통합 게시글
    COUNCIL_POST,      // 자치회 활동 후기
    USER_PROFILE,
    PINECONE_MEMORY,
    MESSAGE            // 쪽지 첨부파일
}
