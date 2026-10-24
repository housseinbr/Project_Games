package com.gamingshop.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatSessionDTO {

	private String id;
	private String clientName;
	private String clientPhone;
	private String status;
	private Boolean isReadByAdmin;
	private LocalDateTime createdAt;
	private List<MessageDTO> messages;
	private Long unreadCount;
	private List<OrderItemDTO> orderItems;

	@Data
	public static class MessageDTO {
		private String id;
		private String senderType;
		private String senderName;
		private String message;
		private Boolean isRead;
		private boolean edited; // ← NOUVEAU
		private LocalDateTime sentAt;
	}

	@Data
	public static class OrderItemDTO {
		private String id;
		private String productName;
		private Integer quantity;
		private Double unitPrice;
	}
}