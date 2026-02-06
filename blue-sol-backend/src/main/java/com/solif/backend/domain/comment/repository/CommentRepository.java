package com.solif.backend.domain.comment.repository;

import com.solif.backend.domain.comment.dto.PostCommentCount;
import com.solif.backend.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글별 댓글 목록 조회
    List<Comment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);

    // 게시글별 댓글 수 조회
    Long countByPost_PostId(Long postId);

    // 여러 게시글의 댓글 수 한 번에 조회
    @Query("SELECT new com.solif.backend.domain.comment.dto.PostCommentCount(c.post.postId, COUNT(c)) " +
            "FROM Comment c " +
            "WHERE c.post.postId IN :postIds " +
            "GROUP BY c.post.postId")
    List<PostCommentCount> countByPostIds(@Param("postIds") List<Long> postIds);

    // Map으로 변환하는 편의 메서드
    default Map<Long, Long> countByPostIdsAsMap(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return new HashMap<>();
        }

        List<PostCommentCount> results = countByPostIds(postIds);
        return results.stream()
                .collect(Collectors.toMap(
                        PostCommentCount::getPostId,
                        PostCommentCount::getCommentCount
                ));
    }

    // 사용자가 첫 번째 작성한 댓글
    Optional<Comment> findFirstByUser_UserIdOrderByCreatedAtAsc(Long userId);
}