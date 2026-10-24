package com.gamingshop.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Sous-document embarqué dans ChatSession.orderItems (pas de collection
 * propre).
 */
@Data
public class ChatOrderItem {

	private String id;

	// Référence simple vers Product (id string), plus d'objet Product complet
	private String productId;

	private String productName;

	private Integer quantity;

	private BigDecimal unitPrice;
}
