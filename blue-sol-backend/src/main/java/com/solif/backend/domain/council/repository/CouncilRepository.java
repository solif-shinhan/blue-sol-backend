package com.solif.backend.domain.council.repository;

import com.solif.backend.domain.council.entity.Council;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouncilRepository extends JpaRepository<Council, Long> {
}