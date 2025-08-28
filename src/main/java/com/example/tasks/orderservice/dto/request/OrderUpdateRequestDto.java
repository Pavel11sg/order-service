package com.example.tasks.orderservice.dto.request;

import com.example.tasks.orderservice.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderUpdateRequestDto {
	@NotNull(message = "Status is required")
	private OrderStatus status;
}
