package com.example.tasks.orderservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequestDto {
	@NotNull(message = "User ID is required")
	private UUID userId;

	@NotEmpty(message = "Order must contain at least one item")
	private List<OrderItemRequestDto> items;
}
