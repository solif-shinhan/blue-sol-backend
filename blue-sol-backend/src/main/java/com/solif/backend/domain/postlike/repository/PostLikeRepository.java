package com.solif.backend.domain.postlike.repository;

import com.solif.backend.domain.postlike.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 좋아요 존재 여부 확인
    boolean existsByUser_UserIdAndPost_PostId(Long userId, Long postId);

    // 좋아요 조회 (삭제용)
    Optional<PostLike> findByUser_UserIdAndPost_PostId(Long userId, Long postId);

    // 게시글의 좋아요 수
    Long countByPost_PostId(Long postId);

    // 여러 게시글의 좋아요 수를 한 번에 조회 (N+1 방지)
    @Query("SELECT pl.post.postId, COUNT(pl) " +
            "FROM PostLike pl " +
            "WHERE pl.post.postId IN :postIds " +
            "GROUP BY pl.post.postId")
    List<Object[]> countByPostIdsRaw(@Param("postIds") List<Long> postIds);

    default Map<Long, Long> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Object[]> results = countByPostIdsRaw(postIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}