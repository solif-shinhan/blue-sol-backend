package com.solif.backend.domain.noticepost.repository;

import com.solif.backend.domain.noticepost.entity.NoticePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoticePostRepository extends JpaRepository<NoticePost, Long> {

    // Post ID로 조회
    @Query("SELECT np FROM NoticePost np " +
            "WHERE np.post.postId = :postId")
    Optional<NoticePost> findByPostId(@Param("postId") Long postId);

    // 삭제되지 않은 게시글 조회 (상세 조회용)
    @Query("SELECT np FROM NoticePost np " +
            "JOIN FETCH np.post p " +
            "JOIN FETCH p.author " +
            "WHERE np.noticePostId = :noticePostId " +
            "AND np.deletedAt IS NULL")
    Optional<NoticePost> findActiveByIdWithPost(@Param("noticePostId") Long noticePostId);

    // 삭제 여부 무관 조회 (수정/삭제용)
    @Query("SELECT np FROM NoticePost np " +
            "JOIN FETCH np.post p " +
            "JOIN FETCH p.author " +
            "WHERE np.noticePostId = :noticePostId")
    Optional<NoticePost> findByIdWithPost(@Param("noticePostId") Long noticePostId);
}