package com.solif.backend.domain.mission.repository;

import com.solif.backend.domain.mission.entity.Mission;
import com.solif.backend.domain.mission.entity.MissionCategory;
import com.solif.backend.domain.mission.entity.MissionStatus;
import com.solif.backend.domain.mission.entity.UserMission;
import com.solif.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {

    Optional<UserMission> findByUserAndMission(User user, Mission mission);

    List<UserMission> findByUser(User user);

    @Query("SELECT um FROM UserMission um " +
            "JOIN FETCH um.mission m " +
            "WHERE um.user = :user " +
            "ORDER BY m.missionCategory ASC, m.sequenceOrder ASC")
    List<UserMission> findByUserWithMission(@Param("user") User user);

    @Query("SELECT COUNT(um) FROM UserMission um " +
            "WHERE um.user = :user " +
            "AND um.mission.missionCategory = :category " +
            "AND um.status = :status")
    Long countByUserAndCategoryAndStatus(
            @Param("user") User user,
            @Param("category") MissionCategory category,
            @Param("status") MissionStatus status
    );

    @Query("SELECT um FROM UserMission um " +
            "JOIN FETCH um.mission m " +
            "WHERE um.user = :user " +
            "AND m.missionCategory = :category " +
            "ORDER BY m.sequenceOrder ASC")
    List<UserMission> findByUserAndMission_MissionCategory(
            @Param("user") User user,
            @Param("category") MissionCategory category
    );
}