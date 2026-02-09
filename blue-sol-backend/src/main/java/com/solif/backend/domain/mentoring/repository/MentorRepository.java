package com.solif.backend.domain.mentoring.repository;

import com.solif.backend.domain.mentoring.entity.Mentor;
import com.solif.backend.domain.mentoring.entity.MentorCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentorRepository extends JpaRepository<Mentor, Long> {

    // 활성화된 전문가 멘토 전체 조회
    List<Mentor> findByIsActiveTrueOrderByCreatedAtDesc();

    // 카테고리별 활성화된 멘토 목록 조회
    List<Mentor> findByIsActiveTrueAndMentorCategoryOrderByCreatedAtDesc(MentorCategory mentorCategory);
}