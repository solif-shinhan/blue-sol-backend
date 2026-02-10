package com.solif.backend.domain.file.entity;

public enum AttachmentPurpose {
    POST_ATTACHMENT,          // 게시글 첨부 이미지
    RECEIPT,                  // 영수증 (OCR용)
    PINECONE_MEMORY_IMAGE,    // 솔방울 추억 이미지
    MESSAGE_ATTACHMENT,       // 쪽지
    MENTORING_CARD_ATTACHMENT // 멘토링 엽서 첨부파일
}
