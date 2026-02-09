package com.solif.backend.domain.mentoring.repository;

import com.solif.backend.domain.mentoring.entity.MentoringInteraction;
import com.solif.backend.domain.mentoring.entity.MentoringInteractionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentoringInteractionRepository extends JpaRepository<MentoringInteraction, Long> {

    // 특정 사용자 간 상호작용 존재 여부 확인
    boolean existsBySender_UserIdAndReceiver_UserIdAndInteractionType(
            Long senderId, Long receiverId, MentoringInteractionType interactionType);

    // 양방향 상호작용 체크 (서로 보냈는지 확인)
    @Query("SELECT CASE WHEN COUNT(mi) = 2 THEN true ELSE false END " +
            "FROM MentoringInteraction mi " +
            "WHERE ((mi.sender.userId = :userId1 AND mi.receiver.userId = :userId2) OR " +
            "       (mi.sender.userId = :userId2 AND mi.receiver.userId = :userId1)) " +
            "AND mi.interactionType = :type")
    boolean existsMutualInteraction(@Param("userId1") Long userId1,
                                    @Param("userId2") Long userId2,
                                    @Param("type") MentoringInteractionType type);

    // 특정 사용자에게 온 상호작용 목록 (받은 것만)
    @Query("SELECT mi FROM MentoringInteraction mi " +
            "JOIN FETCH mi.sender " +
            "WHERE mi.receiver.userId = :userId " +
            "AND mi.interactionType = :type " +
            "ORDER BY mi.createdAt DESC")
    List<MentoringInteraction> findReceivedInteractions(@Param("userId") Long userId,
                                                        @Param("type") MentoringInteractionType type);

    // 응원하기: 내가 받은 것 중 아직 내가 보내지 않은 사람들 (대기중)
    @Query("SELECT mi FROM MentoringInteraction mi " +
            "JOIN FETCH mi.sender " +
            "WHERE mi.receiver.userId = :userId " +
            "AND mi.interactionType = :type " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM MentoringInteraction mi2 " +
            "  WHERE mi2.sender.userId = :userId " +
            "  AND mi2.receiver.userId = mi.sender.userId " +
            "  AND mi2.interactionType = :type" +
            ") " +
            "ORDER BY mi.createdAt DESC")
    List<MentoringInteraction> findPendingReceivedInteractions(@Param("userId") Long userId,
                                                               @Param("type") MentoringInteractionType type);

    // 양방향 완료된 상호작용 (서로 보낸 사람들)
    @Query("SELECT mi FROM MentoringInteraction mi " +
            "JOIN FETCH mi.sender " +
            "JOIN FETCH mi.receiver " +
            "WHERE ((mi.sender.userId = :userId OR mi.receiver.userId = :userId)) " +
            "AND mi.interactionType = :type " +
            "AND EXISTS (" +
            "  SELECT 1 FROM MentoringInteraction mi2 " +
            "  WHERE mi2.sender.userId = mi.receiver.userId " +
            "  AND mi2.receiver.userId = mi.sender.userId " +
            "  AND mi2.interactionType = :type" +
            ") " +
            "ORDER BY mi.createdAt DESC")
    List<MentoringInteraction> findCompletedInteractions(@Param("userId") Long userId,
                                                         @Param("type") MentoringInteractionType type);
}