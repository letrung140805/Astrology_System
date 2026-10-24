package com.example.astrology.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.astrology.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	Optional<Conversation> findByIdAndUser_Id(Long id, Long userId);

	List<Conversation> findAllByUser_IdOrderByUpdatedAtDesc(Long userId);
}
