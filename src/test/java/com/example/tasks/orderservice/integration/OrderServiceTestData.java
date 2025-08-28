package com.example.tasks.orderservice.integration;

import com.example.tasks.orderservice.dto.request.OrderCreateRequestDto;
import com.example.tasks.orderservice.dto.request.OrderItemRequestDto;
import com.example.tasks.orderservice.dto.response.UserResponseDto;
import com.example.tasks.orderservice.model.Item;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class OrderServiceTestData {
	public static final UUID USER_ID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID USER_ID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

	public static final String USER_NAME = "John";
	public static final String USER_SURNAME = "Doe";
	public static final String USER_EMAIL = "john@example.com";

	public static final BigDecimal ITEM_PRICE = BigDecimal.valueOf(100.0);
	public static final String ITEM_NAME = "Test Item";
	public static final String ITEM_DESCRIPTION = "Test Description";

	private OrderServiceTestData() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Item createItem(String name, BigDecimal price, int stockQuantity) {
		Item item = new Item();
		item.setName(name);
		item.setPrice(price);
		item.setDescription(ITEM_DESCRIPTION);
		item.setStockQuantity(stockQuantity);
		return item;
	}

	public static Item createDefaultItem() {
		return createItem(ITEM_NAME, ITEM_PRICE, 10);
	}

	public static Item createItemWithStock(int stockQuantity) {
		return createItem(ITEM_NAME, ITEM_PRICE, stockQuantity);
	}

	public static Item createItemWithCustomStock(UUID itemId, int stockQuantity) {
		return createItem(ITEM_NAME, ITEM_PRICE, stockQuantity);
	}

	public static UserResponseDto createUserResponse(UUID userId) {
		return new UserResponseDto(
				userId, USER_NAME, USER_SURNAME, USER_EMAIL,
				LocalDate.of(1990, 1, 1), List.of()
		);
	}

	public static UserResponseDto createDefaultUserResponse() {
		return createUserResponse(USER_ID_1);
	}

	public static UserResponseDto createUserResponse(UUID userId, String name, String surname) {
		return new UserResponseDto(
				userId, name, surname, USER_EMAIL,
				LocalDate.of(1990, 1, 1), List.of()
		);
	}

	public static OrderCreateRequestDto createOrderRequest(UUID userId, UUID itemId, int quantity) {
		return new OrderCreateRequestDto(
				userId,
				List.of(new OrderItemRequestDto(itemId, quantity))
		);
	}

	public static OrderCreateRequestDto createOrderRequest(UUID userId, List<OrderItemRequestDto> items) {
		return new OrderCreateRequestDto(userId, items);
	}

	public static OrderCreateRequestDto createDefaultOrderRequest(UUID itemId) {
		return createOrderRequest(USER_ID_1, itemId, 3);
	}

	public static OrderCreateRequestDto createOrderRequestWithInsufficientStock(UUID itemId) {
		return createOrderRequest(USER_ID_1, itemId, 10000);
	}

	public static OrderCreateRequestDto createOrderRequestWithMultipleItems(UUID userId, UUID itemId1, int quantity1, UUID itemId2, int quantity2) {
		return new OrderCreateRequestDto(
				userId,
				List.of(
						new OrderItemRequestDto(itemId1, quantity1),
						new OrderItemRequestDto(itemId2, quantity2)
				)
		);
	}

	public static OrderCreateRequestDto createOrderRequestWithNullUserId(UUID itemId) {
		return new OrderCreateRequestDto(null, List.of(new OrderItemRequestDto(itemId, 1)));
	}

	public static OrderCreateRequestDto createOrderRequestWithEmptyItems(UUID userId) {
		return new OrderCreateRequestDto(userId, List.of());
	}
}