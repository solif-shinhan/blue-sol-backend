package com.solif.backend.domain.file.entity;

public enum AttachmentPurpose {
    PROFILE_IMAGE,           // 프로필 이미지
    PINECONE_MEMORY_IMAGE,   // 솔방울 추억 이미지
    POST_ATTACHMENT,         // 게시글 첨부 이미지
    RECEIPT,                 // 영수증 (OCR용)
    MESSAGE_ATTACHMENT       // 쪽지
}
