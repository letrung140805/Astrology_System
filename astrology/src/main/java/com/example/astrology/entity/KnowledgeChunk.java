package com.example.astrology.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "knowledge_chunks", uniqueConstraints = {
		@UniqueConstraint(name = "uk_document_chunk", columnNames = { "document_id", "chunk_index" })
}, indexes = {
		@Index(name = "idx_chunks_document", columnList = "document_id")
})
@Getter
@Setter
@NoArgsConstructor
public class KnowledgeChunk {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private AstrologyKnowledge document;

	@Column(name = "chunk_index", nullable = false)
	private Integer chunkIndex;

	@Column(name = "chunk_title", length = 255)
	private String chunkTitle;

	@Column(name = "chunk_content", nullable = false, columnDefinition = "TEXT")
	private String chunkContent;

	@Column(length = 500)
	private String keywords;

	@Column(name = "embedding_reference", length = 255)
	private String embeddingReference;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
