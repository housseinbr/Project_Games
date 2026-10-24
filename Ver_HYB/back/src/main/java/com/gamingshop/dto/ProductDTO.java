package com.gamingshop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
	private String id;
	private String name;
	private String description;
	private BigDecimal price;
	private Integer stock;
	private String imageUrl;
	private String categoryId;
	private String categoryName;

	// ===== NOUVEAUX CHAMPS =====
	private String regions; // "Global,EU,US,TR"
	private String valuesAvailable; // "10$,20$,50$,100$"
	private String pricesByValue; // JSON string {"10$":32,"20$":62}

	// Helpers pour le frontend (listes parsées)
	private List<String> regionList;
	private List<String> valueList;
}