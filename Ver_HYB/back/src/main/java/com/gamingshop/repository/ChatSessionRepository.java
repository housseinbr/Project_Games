package com.gamingshop.repository;

import com.gamingshop.entity.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {

	List<ChatSession> findByOrderByCreatedAtDesc();

	// Retrouve la session qui contient un message embarqué donné (messages.id = ?0)
	Optional<ChatSession> findByMessagesId(String messageId);

	@Query(value = "{ 'isReadByAdmin': false, 'status': 'PENDING' }", count = true)
	Long countUnreadSessions();

	List<ChatSession> findByIsReadByAdminFalseOrderByCreatedAtDesc();

	List<ChatSession> findByClientPhoneOrderByCreatedAtDesc(String clientPhone);

	// Chercher par nom du client (= username du compte)
	List<ChatSession> findByClientNameOrderByCreatedAtDesc(String clientName);
}
