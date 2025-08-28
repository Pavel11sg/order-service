package com.example.tasks.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderItemResponseDto {
	private UUID id;
	private UUID itemId;
	private String itemName;
	private Integer quantity;
	private BigDecimal price;
	private BigDecimal subtotal;
}
