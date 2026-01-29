package com.solif.backend.domain.profile.repository;

import com.solif.backend.domain.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser_UserId(Long userId);

    boolean existsByUser_UserId(Long userId);
}
