package com.example.astrology.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.astrology.entity.KnowledgeChunk;

public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

	List<KnowledgeChunk> findByDocument_Status(String status);

	@Query("SELECT c FROM KnowledgeChunk c WHERE c.document.status = 'ACTIVE' AND ("
			+ "LOWER(c.chunkTitle) LIKE LOWER(CONCAT('%', :term, '%')) OR "
			+ "LOWER(c.keywords) LIKE LOWER(CONCAT('%', :term, '%')) OR "
			+ "LOWER(c.chunkContent) LIKE LOWER(CONCAT('%', :term, '%')))")
	List<KnowledgeChunk> searchByTerm(@Param("term") String term);
}

