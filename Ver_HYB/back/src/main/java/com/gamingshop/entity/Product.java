package com.gamingshop.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "products")
@Data
@NoArgsConstructor
public class Product {
	@Id
	private String id;

	private String name;
	private String description;
	private BigDecimal price;
	private Integer stock = 0;

	@Field("image_url")
	private String imageUrl;

	// Référence simple vers Category (pas de jointure native en Mongo)
	@Indexed
	@Field("category_id")
	private String categoryId;

	// Dénormalisé pour éviter un lookup à chaque lecture (mis à jour à l'écriture)
	@Field("category_name")
	private String categoryName;

	// Régions disponibles (ex: "Global,EU,US,TR")
	private String regions;

	// Valeurs disponibles (ex: "10$,20$,50$,100$")
	@Field("values_available")
	private String valuesAvailable;

	// Prix par valeur en JSON (ex: {"10$":32,"20$":62,"50$":150})
	@Field("prices_by_value")
	private String pricesByValue;

	@Field("created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Field("updated_at")
	private LocalDateTime updatedAt = LocalDateTime.now();
}
