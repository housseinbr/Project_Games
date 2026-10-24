package com.gamingshop.controller;

import com.gamingshop.entity.Role;
import com.gamingshop.entity.User;
import com.gamingshop.dto.ApiResponse;
import com.gamingshop.dto.AuthRequest;
import com.gamingshop.dto.AuthResponse;
import com.gamingshop.dto.UpdateProfileRequest;
import com.gamingshop.dto.UserProfileDTO;
import com.gamingshop.security.JwtUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.gamingshop.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository adminRepository;

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest request) {
		try {
			// LOG : vérifie ce qui est en base

			System.out.println("Nouveau hash: " + passwordEncoder.encode("admin123"));

			adminRepository.findByUsername(request.getUsername()).ifPresentOrElse(admin -> {
				System.out.println("=== DEBUG ===");
				System.out.println("Username trouvé: " + admin.getUsername());
				System.out.println("Hash en base: " + admin.getPassword());
				System.out.println("Password reçu: " + request.getPassword());
				System.out.println(
						"Match BCrypt: " + passwordEncoder.matches(request.getPassword(), admin.getPassword()));
				System.out.println("=============");
			}, () -> System.out.println("=== USER NON TROUVÉ: " + request.getUsername() + " ==="));

			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
			User user = adminRepository.findByUsername(request.getUsername()).orElseThrow();
			String role = "ROLE_" + user.getRole().name(); // ROLE_ADMIN ou ROLE_CLIENT
			String token = jwtUtil.generateToken(request.getUsername(), List.of(role));
			AuthResponse response = new AuthResponse(token, request.getUsername(), "Login successful");
			return ResponseEntity.ok(ApiResponse.success("Login successful", response));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(401).body(ApiResponse.error("Invalid username or password"));
		}
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<String>> register(@RequestBody AuthRequest request) {

		if (adminRepository.findByUsername(request.getUsername()).isPresent()) {
			return ResponseEntity.badRequest().body(ApiResponse.error("Username already exists"));
		}

		User user = new User();
		user.setUsername(request.getUsername());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(Role.CLIENT); // 🔥 toujours client

		adminRepository.save(user);

		return ResponseEntity.ok(ApiResponse.success("User registered successfully", "OK"));
	}

	// ============================================================
	// PROFIL DE L'UTILISATEUR CONNECTÉ (admin ou client)
	// ============================================================

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserProfileDTO>> getMe(Authentication authentication) {
		User user = adminRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		return ResponseEntity.ok(ApiResponse.success("OK", toProfileDTO(user)));
	}

	@PutMapping("/me")
	public ResponseEntity<ApiResponse<UserProfileDTO>> updateMe(Authentication authentication,
			@RequestBody UpdateProfileRequest request) {

		User user = adminRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// ── Changement de nom d'utilisateur ──
		if (request.getUsername() != null && !request.getUsername().isBlank()
				&& !request.getUsername().equals(user.getUsername())) {
			if (adminRepository.findByUsername(request.getUsername()).isPresent()) {
				return ResponseEntity.badRequest().body(ApiResponse.error("Ce nom d'utilisateur est déjà pris"));
			}
			user.setUsername(request.getUsername());
		}

		// ── Changement d'email ──
		if (request.getEmail() != null && !request.getEmail().isBlank()) {
			user.setEmail(request.getEmail());
		}

		// ── Changement de mot de passe (exige le mot de passe actuel) ──
		if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
			if (request.getCurrentPassword() == null
					|| !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
				return ResponseEntity.badRequest().body(ApiResponse.error("Mot de passe actuel incorrect"));
			}
			user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		}

		adminRepository.save(user);

		UserProfileDTO dto = toProfileDTO(user);
		// Nouveau token (l'ancien contenait peut-être l'ancien username, donc plus
		// valide)
		String role = "ROLE_" + user.getRole().name();
		dto.setToken(jwtUtil.generateToken(user.getUsername(), List.of(role)));

		return ResponseEntity.ok(ApiResponse.success("Profil mis à jour avec succès", dto));
	}

	private UserProfileDTO toProfileDTO(User user) {
		UserProfileDTO dto = new UserProfileDTO();
		dto.setId(user.getId());
		dto.setUsername(user.getUsername());
		dto.setEmail(user.getEmail());
		dto.setRole(user.getRole().name());
		return dto;
	}
}