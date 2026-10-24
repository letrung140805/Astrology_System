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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages", indexes = {
		@Index(name = "idx_messages_conversation", columnList = "conversation_id"),
		@Index(name = "idx_messages_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "conversation_id", nullable = false)
	private Conversation conversation;

	@Column(name = "sender_type", nullable = false, length = 20)
	private String senderType;

	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String content;

	@Column(name = "message_type", nullable = false, length = 30)
	private String messageType = "TEXT";

	@Column(name = "context_used", columnDefinition = "LONGTEXT")
	private String contextUsed;

	@Column(name = "model_name", length = 100)
	private String modelName;

	@Column(name = "token_count")
	private Integer tokenCount;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
