package com.gamingshop.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "chat_sessions")
@Data
@NoArgsConstructor
public class ChatSession {

	@Id
	private String id;

	@Field("client_name")
	@Indexed
	private String clientName;

	@Field("client_phone")
	@Indexed
	private String clientPhone;

	private SessionStatus status = SessionStatus.PENDING;

	@Field("is_read_by_admin")
	private Boolean isReadByAdmin = false;

	// Embedding : les items de commande et les messages vivent DANS le document
	@Field("order_items")
	private List<ChatOrderItem> orderItems = new ArrayList<>();

	private List<ChatMessage> messages = new ArrayList<>();

	@Field("created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Field("updated_at")
	private LocalDateTime updatedAt = LocalDateTime.now();

	public enum SessionStatus {
		PENDING, ACTIVE, CLOSED
	}
}
