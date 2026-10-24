package com.example.astrology.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "knowledge_documents", indexes = {
		@Index(name = "idx_knowledge_category", columnList = "category"),
		@Index(name = "idx_knowledge_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class AstrologyKnowledge {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(length = 100)
	private String category;

	@Column(length = 255)
	private String source;

	@Column(length = 150)
	private String author;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "document_content", nullable = false, columnDefinition = "LONGTEXT")
	private String documentContent;

	@Column(length = 50)
	private String version;

	@Column(nullable = false, length = 30)
	private String status = "ACTIVE";

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
