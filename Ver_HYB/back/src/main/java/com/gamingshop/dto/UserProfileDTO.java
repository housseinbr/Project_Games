package com.gamingshop.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
	private String id;
	private String username;
	private String email;
	private String role;
	private String token; // nouveau JWT, renvoyé uniquement après une mise à jour (évite la déconnexion)
}
