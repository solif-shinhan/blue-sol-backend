package com.solif.backend.domain.message.repository;

import com.solif.backend.domain.message.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 받은 쪽지 목록 (삭제되지 않은 것만)
    @Query("SELECT m FROM Message m JOIN FETCH m.sender " +
            "WHERE m.receiver.userId = :userId " +
            "AND m.deletedByReceiver = false " +
            "ORDER BY m.createdAt DESC")
    Slice<Message> findReceivedMessages(@Param("userId") Long userId, Pageable pageable);

    // 보낸 쪽지 목록 (삭제되지 않은 것만)
    @Query("SELECT m FROM Message m JOIN FETCH m.receiver " +
            "WHERE m.sender.userId = :userId " +
            "AND m.deletedBySender = false " +
            "ORDER BY m.createdAt DESC")
    Slice<Message> findSentMessages(@Param("userId") Long userId, Pageable pageable);

    // 첫 번째 보낸 쪽지 조회
    Optional<Message> findFirstBySender_UserIdOrderByCreatedAtAsc(Long senderId);

    // 특정 사용자 간 쪽지 존재 여부
    boolean existsBySender_UserIdAndReceiver_UserId(Long senderId, Long receiverId);

    // 특정 사용자 간 첫 쪽지 조회
    Optional<Message> findFirstBySender_UserIdAndReceiver_UserIdOrderByCreatedAtAsc(Long senderId, Long receiverId);

    // sender를 JOIN FETCH하여 N+1 방지
    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.messageId IN :ids")
    List<Message> findAllByIdWithSender(@Param("ids") List<Long> ids);
}
