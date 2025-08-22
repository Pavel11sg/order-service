package com.example.tasks.orderservice.dto.response;

import lombok.Data;

@Data
public class OrderWithUserResponseDto {
	private UserResponseDto user;
	private OrderResponseDto order;

	public OrderWithUserResponseDto(UserResponseDto user, OrderResponseDto order) {
		this.user = user;
		this.order = order;
	}
}
