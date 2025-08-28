package com.example.tasks.orderservice.dto.response;

import com.example.tasks.orderservice.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderResponseDto {
	private UUID orderId;
	private UUID userId;
	private OrderStatus status;
	private BigDecimal totalAmount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<OrderItemResponseDto> items;
}
