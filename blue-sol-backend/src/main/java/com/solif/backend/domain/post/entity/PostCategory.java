package com.solif.backend.domain.post.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCategory {

    // 고민상담 카테고리
    STUDY("학업"),
    ADMISSION("진학"),
    JOB("취업"),
    ETC("기타"),

    // 장학재단 소식 카테고리
    NOTICE("운영공지"),
    PROGRAM("프로그램"),

    // 장학 프로그램 카테고리
    REQUIRED("필수 프로그램"),
    OPTIONAL("선택 프로그램");

    private final String description;
}