package com.solif.backend.domain.board.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardType {

    WARM_ACTIVITY("따뜻한 활동 후기"),
    WORRY_TALK("토닥토닥 고민상담"),
    FOUNDATION_NEWS("장학재단 소식");

    private final String description;
}