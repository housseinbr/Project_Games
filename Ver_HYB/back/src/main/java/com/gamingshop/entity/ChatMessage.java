package com.gamingshop.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Sous-document embarqué dans ChatSession.messages (pas de collection propre).
 */
@Data
public class ChatMessage {

	// Identifiant généré manuellement (ObjectId.toHexString()) car ce n'est pas
	// un document racine géré par un repository.
	private String id;

	private SenderType senderType;

	private String senderName;

	private String message;

	private Boolean isRead = false;

	private boolean edited = false;

	private LocalDateTime sentAt = LocalDateTime.now();

	public enum SenderType {
		CLIENT, ADMIN
	}
}
