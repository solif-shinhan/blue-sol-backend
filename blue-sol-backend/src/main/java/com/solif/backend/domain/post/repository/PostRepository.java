package com.solif.backend.domain.post.repository;

import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.entity.PostCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시판별 목록 조회 (삭제되지 않은 것만)
    Slice<Post> findByBoard_BoardIdAndDeletedAtIsNull(Long boardId, Pageable pageable);

    // 게시판 + 카테고리별 목록 조회
    Slice<Post> findByBoard_BoardIdAndPostCategoryAndDeletedAtIsNull(
            Long boardId,
            PostCategory postCategory,
            Pageable pageable
    );
}