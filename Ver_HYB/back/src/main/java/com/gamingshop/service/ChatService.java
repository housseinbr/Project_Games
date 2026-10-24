package com.gamingshop.service;

import com.gamingshop.dto.ChatSessionDTO;
import com.gamingshop.dto.CreateSessionRequest;
import com.gamingshop.dto.SendMessageRequest;
import com.gamingshop.entity.ChatMessage;
import com.gamingshop.entity.ChatOrderItem;
import com.gamingshop.entity.ChatSession;
import com.gamingshop.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatSessionRepository sessionRepository;
	private final SimpMessagingTemplate messagingTemplate;

	public ChatSessionDTO createSession(CreateSessionRequest request) {
		ChatSession session = new ChatSession();
		// Génère l'id à l'avance, MongoRepository.save() respecte un id déjà positionné
		session.setId(new ObjectId().toHexString());
		session.setClientName(request.getClientName());
		session.setClientPhone(request.getClientPhone());
		session.setStatus(ChatSession.SessionStatus.PENDING);
		session.setIsReadByAdmin(false);

		if (request.getItems() != null) {
			for (CreateSessionRequest.OrderItemRequest item : request.getItems()) {
				ChatOrderItem orderItem = new ChatOrderItem();
				orderItem.setId(new ObjectId().toHexString());
				orderItem.setProductId(item.getProductId());
				orderItem.setProductName(item.getProductName());
				orderItem.setQuantity(item.getQuantity());
				orderItem.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
				session.getOrderItems().add(orderItem);
			}
		}

		String orderSummary = buildOrderSummary(request);
		ChatMessage autoMsg = new ChatMessage();
		autoMsg.setId(new ObjectId().toHexString());
		autoMsg.setSenderType(ChatMessage.SenderType.CLIENT);
		autoMsg.setSenderName(request.getClientName());
		autoMsg.setMessage(orderSummary);
		autoMsg.setIsRead(false);
		autoMsg.setSentAt(LocalDateTime.now());
		session.getMessages().add(autoMsg);

		session = sessionRepository.save(session);
		String sessionId = session.getId();

		messagingTemplate.convertAndSend("/topic/admin/notifications", Map.of("type", "NEW_SESSION", "sessionId",
				sessionId, "clientName", request.getClientName(), "phone", request.getClientPhone()));

		return getSessionById(sessionId);
	}

	private String buildOrderSummary(CreateSessionRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append("Bonjour, je suis ").append(request.getClientName());
		sb.append(" (").append(request.getClientPhone()).append("). Je voudrais commander:\n");
		if (request.getItems() != null) {
			for (CreateSessionRequest.OrderItemRequest item : request.getItems()) {
				sb.append("• ").append(item.getProductName()).append(" × ").append(item.getQuantity()).append(" — ")
						.append(String.format("%.2f", item.getUnitPrice() * item.getQuantity())).append(" DT\n");
			}
		}
		sb.append("\nComment puis-je payer ?");
		return sb.toString();
	}

	public ChatSessionDTO sendMessage(SendMessageRequest request) {
		ChatSession session = sessionRepository.findById(request.getSessionId())
				.orElseThrow(() -> new RuntimeException("Session not found"));

		ChatMessage message = new ChatMessage();
		message.setId(new ObjectId().toHexString());
		message.setSenderType(ChatMessage.SenderType.valueOf(request.getSenderType()));
		message.setSenderName(request.getSenderName());
		message.setMessage(request.getMessage());
		message.setIsRead(false);
		message.setSentAt(LocalDateTime.now());
		session.getMessages().add(message);

		if (session.getStatus() == ChatSession.SessionStatus.PENDING) {
			session.setStatus(ChatSession.SessionStatus.ACTIVE);
		}
		if ("CLIENT".equals(request.getSenderType())) {
			session.setIsReadByAdmin(false);
		}
		session.setUpdatedAt(LocalDateTime.now());
		sessionRepository.save(session);

		ChatSessionDTO.MessageDTO msgDTO = new ChatSessionDTO.MessageDTO();
		msgDTO.setId(message.getId());
		msgDTO.setSenderType(request.getSenderType());
		msgDTO.setSenderName(request.getSenderName());
		msgDTO.setMessage(request.getMessage());
		msgDTO.setSentAt(message.getSentAt());

		messagingTemplate.convertAndSend("/topic/chat/" + request.getSessionId(), msgDTO);

		if ("CLIENT".equals(request.getSenderType())) {
			messagingTemplate.convertAndSend("/topic/admin/notifications", Map.of("type", "NEW_MESSAGE", "sessionId",
					request.getSessionId(), "senderName", request.getSenderName(), "message", request.getMessage()));
		}

		return getSessionById(request.getSessionId());
	}

	// =========================================================
	// Modifier un message (admin seulement)
	// =========================================================
	public ChatSessionDTO editMessage(String messageId, String newText) {
		ChatSession session = sessionRepository.findByMessagesId(messageId)
				.orElseThrow(() -> new RuntimeException("Message not found"));

		ChatMessage message = session.getMessages().stream().filter(m -> m.getId().equals(messageId)).findFirst()
				.orElseThrow(() -> new RuntimeException("Message not found"));

		message.setMessage(newText);
		message.setEdited(true);
		sessionRepository.save(session);

		messagingTemplate.convertAndSend("/topic/chat/" + session.getId(),
				Map.of("type", "MESSAGE_EDITED", "messageId", messageId, "newText", newText));

		return getSessionById(session.getId());
	}

	// =========================================================
	// Supprimer un message (admin seulement)
	// =========================================================
	public ChatSessionDTO deleteMessage(String messageId) {
		ChatSession session = sessionRepository.findByMessagesId(messageId)
				.orElseThrow(() -> new RuntimeException("Message not found"));

		session.getMessages().removeIf(m -> m.getId().equals(messageId));
		sessionRepository.save(session);

		messagingTemplate.convertAndSend("/topic/chat/" + session.getId(),
				Map.of("type", "MESSAGE_DELETED", "messageId", messageId));

		return getSessionById(session.getId());
	}

	public ChatSessionDTO getSessionById(String id) {
		ChatSession session = sessionRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Session not found"));
		return toDTO(session);
	}

	public List<ChatSessionDTO> getAllSessions() {
		return sessionRepository.findByOrderByCreatedAtDesc().stream().map(this::toDTO).collect(Collectors.toList());
	}

	public Long getUnreadCount() {
		return sessionRepository.countUnreadSessions();
	}

	public void markSessionAsRead(String sessionId) {
		ChatSession session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("Session not found"));
		session.setIsReadByAdmin(true);
		session.getMessages().forEach(m -> m.setIsRead(true));
		sessionRepository.save(session);
	}

	public void closeAndDeleteSession(String sessionId) {
		if (!sessionRepository.existsById(sessionId)) {
			throw new RuntimeException("Session not found");
		}

		// Notifier WebSocket avant suppression
		messagingTemplate.convertAndSend("/topic/chat/" + sessionId, Map.of("type", "SESSION_CLOSED"));

		// Un seul document à supprimer : messages et items embarqués partent avec lui
		sessionRepository.deleteById(sessionId);
	}

	public List<ChatSessionDTO> getSessionsByPhone(String clientPhone) {
		return sessionRepository.findByClientPhoneOrderByCreatedAtDesc(clientPhone).stream().map(this::toDTO)
				.collect(Collectors.toList());
	}

	public List<ChatSessionDTO> getMySessionsByUsername(String username) {
		return sessionRepository.findByClientNameOrderByCreatedAtDesc(username).stream().map(this::toDTO)
				.collect(Collectors.toList());
	}

	private ChatSessionDTO toDTO(ChatSession session) {
		ChatSessionDTO dto = new ChatSessionDTO();
		dto.setId(session.getId());
		dto.setClientName(session.getClientName());
		dto.setClientPhone(session.getClientPhone());
		dto.setStatus(session.getStatus().name());
		dto.setIsReadByAdmin(session.getIsReadByAdmin());
		dto.setCreatedAt(session.getCreatedAt());

		List<ChatMessage> messages = session.getMessages().stream()
				.sorted((a, b) -> a.getSentAt().compareTo(b.getSentAt())).collect(Collectors.toList());

		dto.setMessages(messages.stream().map(m -> {
			ChatSessionDTO.MessageDTO msg = new ChatSessionDTO.MessageDTO();
			msg.setId(m.getId());
			msg.setSenderType(m.getSenderType().name());
			msg.setSenderName(m.getSenderName());
			msg.setMessage(m.getMessage());
			msg.setIsRead(m.getIsRead());
			msg.setSentAt(m.getSentAt());
			msg.setEdited(m.isEdited());
			return msg;
		}).collect(Collectors.toList()));

		dto.setOrderItems(session.getOrderItems().stream().map(i -> {
			ChatSessionDTO.OrderItemDTO item = new ChatSessionDTO.OrderItemDTO();
			item.setId(i.getId());
			item.setProductName(i.getProductName());
			item.setQuantity(i.getQuantity());
			item.setUnitPrice(i.getUnitPrice() != null ? i.getUnitPrice().doubleValue() : null);
			return item;
		}).collect(Collectors.toList()));

		long unread = session.getMessages().stream()
				.filter(m -> !Boolean.TRUE.equals(m.getIsRead()) && m.getSenderType() != ChatMessage.SenderType.ADMIN)
				.count();
		dto.setUnreadCount(unread);

		return dto;
	}
}