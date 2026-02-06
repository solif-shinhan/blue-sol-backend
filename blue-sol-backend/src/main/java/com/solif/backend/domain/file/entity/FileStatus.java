package com.solif.backend.domain.file.entity;

public enum FileStatus {
    TEMP,       // 임시 업로드 (24시간 후 삭제 대상)
    PERMANENT   // 확정됨 (게시글 등에 연결됨)
}
