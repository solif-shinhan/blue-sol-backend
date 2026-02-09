package com.solif.backend.domain.mentoring.repository;

import com.solif.backend.domain.mentoring.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentorRepository extends JpaRepository<Mentor, Long> {

    // 활성화된 전문가 멘토 전체 조회
    List<Mentor> findByIsActiveTrueOrderByCreatedAtDesc();
}