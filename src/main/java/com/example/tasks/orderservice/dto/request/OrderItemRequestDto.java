package com.example.tasks.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderItemRequestDto {
	@NotNull(message = "Item ID is required")
	private UUID itemId;

	@NotNull(message = "Quantity is required")
	@Positive(message = "Quantity must be greater than 0")
	private Integer quantity;
}
