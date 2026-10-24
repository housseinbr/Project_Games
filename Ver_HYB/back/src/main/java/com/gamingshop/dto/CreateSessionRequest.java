package com.gamingshop.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateSessionRequest {
	private String clientName;
	private String clientPhone;
	private List<OrderItemRequest> items;

	@Data
	public static class OrderItemRequest {
		private String productId;
		private String productName;
		private Integer quantity;
		private Double unitPrice;
	}
}