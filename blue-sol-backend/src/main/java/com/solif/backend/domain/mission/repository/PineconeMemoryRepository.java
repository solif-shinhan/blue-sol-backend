package com.solif.backend.domain.mission.repository;

import com.solif.backend.domain.mission.entity.PineconeMemory;
import com.solif.backend.domain.mission.entity.UserPinecone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PineconeMemoryRepository extends JpaRepository<PineconeMemory, Long> {

    List<PineconeMemory> findByUserPinecone(UserPinecone userPinecone);

    List<PineconeMemory> findByUserPineconeOrderByCreatedAtAsc(UserPinecone userPinecone);
}