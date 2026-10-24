package com.gamingshop.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
@Data
@NoArgsConstructor
public class Category {
	@Id
	private String id;

	private String name;
	private String description;
	private String icon;

	// ===== Sous-catégories =====
	// null = catégorie de premier niveau (ex: "Carte cadeau")
	// sinon = id de la catégorie parente (ex: sous-catégorie "Carte PlayStation" ->
	// parentId = id de "Carte cadeau")
	private String parentId;

	// Dénormalisé pour éviter un lookup à chaque lecture
	private String parentName;
}
