package com.solif.backend.domain.mission.repository;

import com.solif.backend.domain.mission.entity.MissionCategory;
import com.solif.backend.domain.mission.entity.UserPinecone;
import com.solif.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPineconeRepository extends JpaRepository<UserPinecone, Long> {

    List<UserPinecone> findByUserAndSeasonKey(User user, String seasonKey);

    Optional<UserPinecone> findByUserAndPineconeCategoryAndSeasonKey(
            User user, MissionCategory category, String seasonKey
    );

    boolean existsByUserAndPineconeCategoryAndSeasonKey(
            User user, MissionCategory category, String seasonKey
    );

    Long countByUserAndSeasonKey(User user, String seasonKey);
}