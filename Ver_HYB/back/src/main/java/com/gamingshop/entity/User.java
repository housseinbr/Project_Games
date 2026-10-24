package com.gamingshop.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
public class User {

	@Id
	private String id;

	@Indexed(unique = true)
	private String username;

	private String password;

	private String email;

	private Role role; // ADMIN ou CLIENT

	private LocalDateTime createdAt = LocalDateTime.now();
}
