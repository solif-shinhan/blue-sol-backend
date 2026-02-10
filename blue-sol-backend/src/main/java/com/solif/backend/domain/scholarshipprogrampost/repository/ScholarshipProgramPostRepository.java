package com.solif.backend.domain.scholarshipprogrampost.repository;

import com.solif.backend.domain.post.entity.PostCategory;
import com.solif.backend.domain.scholarshipprogrampost.entity.ScholarshipProgramPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScholarshipProgramPostRepository extends JpaRepository<ScholarshipProgramPost, Long> {

    /**
     * 장학 프로그램 카테고리별 목록 조회 (Post, Author fetch join)
     * - N+1 문제 해결을 위한 fetch join
     * - 삭제되지 않은 게시글만 조회
     * - 최신순 정렬
     */
    @Query("SELECT spp FROM ScholarshipProgramPost spp " +
            "JOIN FETCH spp.post p " +
            "JOIN FETCH p.author " +
            "WHERE p.postCategory = :postCategory " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY p.createdAt DESC")
    List<ScholarshipProgramPost> findByPostCategoryWithPostAndAuthor(
            @Param("postCategory") PostCategory postCategory
    );
}