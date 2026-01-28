package com.solif.backend.domain.comment.repository;

import com.solif.backend.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글별 댓글 목록 조회
    List<Comment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);

    // 게시글별 댓글 수 조회
    Long countByPost_PostId(Long postId);
}