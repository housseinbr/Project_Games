package com.gamingshop.controller;

import com.gamingshop.dto.*;
import com.gamingshop.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	// ---- PUBLIC (CLIENT) ----

	@PostMapping("/session")
	public ResponseEntity<ApiResponse<ChatSessionDTO>> createSession(@RequestBody CreateSessionRequest request) {
		return ResponseEntity.ok(ApiResponse.success("Session created", chatService.createSession(request)));
	}

	@PostMapping("/message")
	public ResponseEntity<ApiResponse<ChatSessionDTO>> sendMessage(@RequestBody SendMessageRequest request) {
		return ResponseEntity.ok(ApiResponse.success(chatService.sendMessage(request)));
	}

	@GetMapping("/session/{id}/messages")
	public ResponseEntity<ApiResponse<ChatSessionDTO>> getSession(@PathVariable String id) {
		return ResponseEntity.ok(ApiResponse.success(chatService.getSessionById(id)));
	}

	@GetMapping("/sessions/by-phone")
	public ResponseEntity<ApiResponse<List<ChatSessionDTO>>> getSessionsByPhone(@RequestParam String clientPhone) {
		return ResponseEntity.ok(ApiResponse.success(chatService.getSessionsByPhone(clientPhone)));
	}

	@GetMapping("/my-sessions")
	public ResponseEntity<ApiResponse<List<ChatSessionDTO>>> getMySessions(Authentication authentication) {
		String username = authentication.getName();
		return ResponseEntity.ok(ApiResponse.success(chatService.getMySessionsByUsername(username)));
	}

	// ---- ADMIN ONLY ----

	@GetMapping("/admin/sessions")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<List<ChatSessionDTO>>> getAllSessions() {
		return ResponseEntity.ok(ApiResponse.success(chatService.getAllSessions()));
	}

	@GetMapping("/admin/unread-count")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
		return ResponseEntity.ok(ApiResponse.success(Map.of("count", chatService.getUnreadCount())));
	}

	@PostMapping("/admin/session/{id}/read")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String id) {
		chatService.markSessionAsRead(id);
		return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
	}

	@DeleteMapping("/admin/session/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> closeAndDeleteSession(@PathVariable String id) {
		chatService.closeAndDeleteSession(id);
		return ResponseEntity.ok(ApiResponse.success("Session supprimée", null));
	}

	@PutMapping("/admin/message/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ChatSessionDTO>> editMessage(@PathVariable String id,
			@RequestBody Map<String, String> body) {
		String newText = body.get("message");
		if (newText == null || newText.isBlank()) {
			return ResponseEntity.badRequest().body(ApiResponse.error("Le message ne peut pas être vide"));
		}
		return ResponseEntity.ok(ApiResponse.success("Message modifié", chatService.editMessage(id, newText)));
	}

	@DeleteMapping("/admin/message/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ChatSessionDTO>> deleteMessage(@PathVariable String id) {
		return ResponseEntity.ok(ApiResponse.success("Message supprimé", chatService.deleteMessage(id)));
	}
}