package com.example.astrology.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.astrology.entity.AstrologyKnowledge;

public interface AstrologyKnowledgeRepository extends JpaRepository<AstrologyKnowledge, Long> {

	List<AstrologyKnowledge> findAllByStatusOrderByUpdatedAtDesc(String status);
}
