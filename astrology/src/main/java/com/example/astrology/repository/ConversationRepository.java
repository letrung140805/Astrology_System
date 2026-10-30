package com.example.astrology.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.astrology.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	Optional<Conversation> findByIdAndUser_Id(Long id, Long userId);

	Optional<Conversation> findByIdAndChart_Id(Long id, Long chartId);

	List<Conversation> findAllByChart_IdOrderByUpdatedAtDesc(Long chartId);

	List<Conversation> findAllByUser_IdOrderByUpdatedAtDesc(Long userId);
}
