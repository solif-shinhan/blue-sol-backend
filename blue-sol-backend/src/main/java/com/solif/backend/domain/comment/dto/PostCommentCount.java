package com.solif.backend.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostCommentCount {
    private Long postId;
    private Long commentCount;
}