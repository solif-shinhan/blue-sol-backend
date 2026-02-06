package com.solif.backend.domain.post.repository;

import com.solif.backend.domain.post.entity.Post;
import com.solif.backend.domain.post.entity.PostCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시판별 목록 조회 (Author fetch join 없음 - 익명용)
    @Query("SELECT p FROM Post p " +
            "WHERE p.board.boardId = :boardId " +
            "AND p.deletedAt IS NULL")
    Slice<Post> findByBoard_BoardIdAndDeletedAtIsNull(
            @Param("boardId") Long boardId,
            Pageable pageable
    );

    // 게시판별 목록 조회 (Author fetch join - 실명용)
    @Query("SELECT p FROM Post p JOIN FETCH p.author " +
            "WHERE p.board.boardId = :boardId " +
            "AND p.deletedAt IS NULL")
    Slice<Post> findByBoard_BoardIdAndDeletedAtIsNullWithAuthor(
            @Param("boardId") Long boardId,
            Pageable pageable
    );

    // 게시판 + 카테고리별 목록 조회 (Author fetch join 없음 - 익명용)
    @Query("SELECT p FROM Post p " +
            "WHERE p.board.boardId = :boardId " +
            "AND p.postCategory = :postCategory " +
            "AND p.deletedAt IS NULL")
    Slice<Post> findByBoard_BoardIdAndPostCategoryAndDeletedAtIsNull(
            @Param("boardId") Long boardId,
            @Param("postCategory") PostCategory postCategory,
            Pageable pageable
    );

    // 게시판 + 카테고리별 목록 조회 (Author fetch join - 실명용)
    @Query("SELECT p FROM Post p JOIN FETCH p.author " +
            "WHERE p.board.boardId = :boardId " +
            "AND p.postCategory = :postCategory " +
            "AND p.deletedAt IS NULL")
    Slice<Post> findByBoard_BoardIdAndPostCategoryAndDeletedAtIsNullWithAuthor(
            @Param("boardId") Long boardId,
            @Param("postCategory") PostCategory postCategory,
            Pageable pageable
    );

    // 사용자가 첫 번째 작성한 게시글
    Optional<Post> findFirstByAuthor_UserIdOrderByCreatedAtAsc(Long authorUserId);
}