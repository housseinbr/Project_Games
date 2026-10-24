package com.gamingshop.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
	private String username; // nouveau nom d'utilisateur (optionnel)
	private String email; // nouvel email (optionnel)
	private String currentPassword; // requis uniquement si newPassword est fourni
	private String newPassword; // nouveau mot de passe (optionnel)
}
