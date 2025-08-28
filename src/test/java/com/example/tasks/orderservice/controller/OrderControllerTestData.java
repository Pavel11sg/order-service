package com.example.tasks.orderservice.controller;

import com.example.tasks.orderservice.dto.request.OrderCreateRequestDto;
import com.example.tasks.orderservice.dto.request.OrderItemRequestDto;
import com.example.tasks.orderservice.dto.request.OrderUpdateRequestDto;
import com.example.tasks.orderservice.dto.response.*;
import com.example.tasks.orderservice.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderControllerTestData {
	public static final UUID ORDER_ID_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	public static final UUID USER_ID_1 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
	public static final UUID ITEM_ID_1 = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
	public static final UUID ITEM_ID_2 = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
	public static final UUID ORDER_ITEM_ID_1 = UUID.fromString("523e4567-e89b-12d3-a456-426614174004");
	public static final UUID CARD_ID_1 = UUID.fromString("623e4567-e89b-12d3-a456-426614174005");
	public static final OrderStatus DEFAULT_STATUS = OrderStatus.IN_PROGRESS;


	private OrderControllerTestData() {
	}

	public static OrderCreateRequestDto createOrderRequest() {
		return createOrderRequest(USER_ID_1, List.of(
				createOrderItemRequest(ITEM_ID_1, 2),
				createOrderItemRequest(ITEM_ID_2, 1)
		));
	}

	public static OrderCreateRequestDto createOrderRequest(UUID userId, List<OrderItemRequestDto> items) {
		return new OrderCreateRequestDto(userId, items);
	}

	public static OrderItemRequestDto createOrderItemRequest(UUID itemId, Integer quantity) {
		return new OrderItemRequestDto(itemId, quantity);
	}

	public static OrderUpdateRequestDto createOrderUpdateRequest(OrderStatus status) {
		return new OrderUpdateRequestDto(status);
	}

	public static OrderUpdateRequestDto createOrderUpdateRequest() {
		return createOrderUpdateRequest(DEFAULT_STATUS);
	}

	public static OrderCreateRequestDto createInvalidOrderRequest() {
		OrderCreateRequestDto request = new OrderCreateRequestDto();
		request.setUserId(null);
		request.setItems(List.of());
		return request;
	}

	public static OrderWithUserResponseDto createOrderWithUserResponse() {
		return new OrderWithUserResponseDto(
				createUserResponse(),
				createOrderResponse(ORDER_ID_1, OrderStatus.CREATED)
		);
	}

	public static OrderWithUserResponseDto createOrderWithUserResponse(UUID orderId, OrderStatus status) {
		return new OrderWithUserResponseDto(
				createUserResponse(),
				createOrderResponse(orderId, status)
		);
	}

	public static OrderResponseDto createOrderResponse(UUID orderId, OrderStatus status) {
		return new OrderResponseDto(
				orderId,
				USER_ID_1,
				status,
				new BigDecimal("299.97"),
				LocalDateTime.now().minusHours(1),
				LocalDateTime.now(),
				List.of(
						createOrderItemResponse(ORDER_ITEM_ID_1, ITEM_ID_1, "iPhone 16", 2, new BigDecimal("199.99")),
						createOrderItemResponse(UUID.randomUUID(), ITEM_ID_2, "MacBook Pro", 1, new BigDecimal("999.99"))
				)
		);
	}

	public static OrderItemResponseDto createOrderItemResponse(UUID id, UUID itemId, String itemName,
															   Integer quantity, BigDecimal price) {
		return new OrderItemResponseDto(
				id,
				itemId,
				itemName,
				quantity,
				price,
				price.multiply(BigDecimal.valueOf(quantity))
		);
	}

	public static UserResponseDto createUserResponse() {
		return new UserResponseDto(
				USER_ID_1,
				"John",
				"Doe",
				"j***@example.com",
				LocalDate.of(1990, 1, 15),
				List.of(createCardResponse())
		);
	}

	public static CardResponseDto createCardResponse() {
		return new CardResponseDto(
				CARD_ID_1,
				"1234",
				"J*** D***",
				LocalDate.now().plusYears(2)
		);
	}

	public static OrderCreateRequestDto createOrderRequestWithSingleItem() {
		return createOrderRequest(USER_ID_1, List.of(
				createOrderItemRequest(ITEM_ID_1, 1)
		));
	}

	public static OrderCreateRequestDto createOrderRequestWithLargeQuantity() {
		return createOrderRequest(USER_ID_1, List.of(
				createOrderItemRequest(ITEM_ID_1, 100)
		));
	}

	public static OrderWithUserResponseDto createOrderWithStatus(OrderStatus status) {
		return createOrderWithUserResponse(ORDER_ID_1, status);
	}


	public static List<OrderWithUserResponseDto> createOrderList(int count) {
		return List.of(
				createOrderWithUserResponse(ORDER_ID_1, OrderStatus.CREATED),
				createOrderWithUserResponse(UUID.randomUUID(), OrderStatus.IN_PROGRESS)
		);
	}

	public static List<UUID> createOrderIdList() {
		return List.of(ORDER_ID_1, UUID.randomUUID());
	}
}
