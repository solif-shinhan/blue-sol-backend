package com.solif.backend.domain.file.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileFolder {
    COUNCIL_REVIEW("council-review"),   // 자치회 활동 후기
    POST("post"),                        // 게시판
    MENTORING("mentoring");              // 멘토링

    private final String folderName;
}
