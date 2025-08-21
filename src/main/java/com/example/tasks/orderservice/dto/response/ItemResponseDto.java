package com.example.tasks.orderservice.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ItemResponseDto {
	private UUID itemId;
	private String name;
	private BigDecimal price;
	private String description;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
