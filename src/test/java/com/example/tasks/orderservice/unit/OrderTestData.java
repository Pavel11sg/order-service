package com.example.tasks.orderservice.unit;

import com.example.tasks.orderservice.dto.request.OrderCreateRequestDto;
import com.example.tasks.orderservice.dto.request.OrderItemRequestDto;
import com.example.tasks.orderservice.dto.request.OrderUpdateRequestDto;
import com.example.tasks.orderservice.dto.response.*;
import com.example.tasks.orderservice.model.Item;
import com.example.tasks.orderservice.model.Order;
import com.example.tasks.orderservice.model.OrderItem;
import com.example.tasks.orderservice.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OrderTestData {

	// UUID константы
	public static final UUID USER_ID = UUID.fromString("8d690749-30a7-424b-a995-df1e45e5fc8f");
	public static final UUID ORDER_ID = UUID.fromString("8d690750-30a7-424b-a995-df1e45e5fc8f");
	public static final UUID ITEM_ID_1 = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
	public static final UUID ITEM_ID_2 = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");

	// OrderItems
	public static final OrderItem ORDER_ITEM_1 = createOrderItem(ITEM_ID_1, 1, BigDecimal.valueOf(3900));
	public static final OrderItem ORDER_ITEM_2 = createOrderItem(ITEM_ID_2, 2, BigDecimal.valueOf(4100));
	public static final List<OrderItem> ORDER_ITEMS = List.of(ORDER_ITEM_1, ORDER_ITEM_2);

	// Request DTOs
	public static final OrderItemRequestDto ORDER_ITEM_REQUEST_1 = new OrderItemRequestDto(ITEM_ID_1, 1);
	public static final OrderItemRequestDto ORDER_ITEM_REQUEST_2 = new OrderItemRequestDto(ITEM_ID_2, 2);
	public static final List<OrderItemRequestDto> ORDER_ITEM_REQUESTS = List.of(ORDER_ITEM_REQUEST_1, ORDER_ITEM_REQUEST_2);
	public static final OrderCreateRequestDto ORDER_CREATE_REQUEST = new OrderCreateRequestDto(USER_ID, ORDER_ITEM_REQUESTS);
	public static final OrderUpdateRequestDto ORDER_UPDATE_REQUEST = new OrderUpdateRequestDto(OrderStatus.CONFIRMED);


	public static final OrderItemResponseDto ORDER_ITEM_RESPONSE_1 =
			new OrderItemResponseDto(UUID.randomUUID(), ITEM_ID_1, "iPhone 16", 1, BigDecimal.valueOf(3900), BigDecimal.valueOf(3900));
	public static final OrderItemResponseDto ORDER_ITEM_RESPONSE_2 =
			new OrderItemResponseDto(UUID.randomUUID(), ITEM_ID_2, "HP ProBook 450G", 2, BigDecimal.valueOf(4100), BigDecimal.valueOf(8200));
	public static final List<OrderItemResponseDto> ORDER_ITEM_RESPONSES = List.of(ORDER_ITEM_RESPONSE_1, ORDER_ITEM_RESPONSE_2);

	public static final UserResponseDto USER_RESPONSE = new UserResponseDto(
			USER_ID, "John", "Doe", "J*****e@gmail.com",
			LocalDate.of(1990, Month.JANUARY, 1), Collections.emptyList()
	);

	public static final CardResponseDto CARD_RESPONSE = createCardResponse();

	private OrderTestData() {
	}

	private static CardResponseDto createCardResponse() {
		CardResponseDto card = new CardResponseDto();
		card.setId(UUID.randomUUID());
		card.setLastFourDigits("1234");
		card.setMaskedHolder("J*** D***");
		return card;
	}

	public static Order createOrder(UUID orderId, UUID userId, OrderStatus status, BigDecimal totalAmount) {
		LocalDateTime now = LocalDateTime.now();
		Order order = new Order();
		order.setOrderId(orderId);
		order.setUserId(userId);
		order.setOrderStatus(status);
		order.setTotalAmount(totalAmount);
		order.setCreatedAt(now);
		order.setUpdatedAt(now);
		order.setOrderItems(ORDER_ITEMS);
		return order;
	}

	public static Item createTestItem(String name, BigDecimal price) {
		Item item = new Item();
		item.setName(name);
		item.setPrice(price);
		item.setStockQuantity(10);
		return item;
	}

	public static Order createDefaultOrder() {
		return createOrder(ORDER_ID, USER_ID, OrderStatus.CREATED, BigDecimal.valueOf(12100));
	}

	public static OrderResponseDto createOrderResponseDto(UUID orderId, UUID userId, OrderStatus status) {
		LocalDateTime now = LocalDateTime.now();
		return new OrderResponseDto(
				orderId, userId, status, BigDecimal.valueOf(12100),
				now, now, ORDER_ITEM_RESPONSES
		);
	}

	public static OrderResponseDto createDefaultOrderResponseDto() {
		return createOrderResponseDto(ORDER_ID, USER_ID, OrderStatus.CREATED);
	}

	private static OrderItem createOrderItem(UUID orderItemId, int quantity, BigDecimal price) {
		OrderItem orderItem = new OrderItem();
		orderItem.setId(orderItemId);
		orderItem.setQuantity(quantity);
		orderItem.setPrice(price);
		return orderItem;
	}

	public static OrderWithUserResponseDto createOrderWithUserResponse() {
		return new OrderWithUserResponseDto(USER_RESPONSE, createDefaultOrderResponseDto());
	}

	// Helper methods for testing
	public static OrderCreateRequestDto createOrderRequestWithNullUserId() {
		return new OrderCreateRequestDto(null, ORDER_ITEM_REQUESTS);
	}

	public static OrderCreateRequestDto createOrderRequestWithEmptyItems() {
		return new OrderCreateRequestDto(USER_ID, List.of());
	}

	public static OrderUpdateRequestDto createOrderUpdateRequest(OrderStatus status) {
		return new OrderUpdateRequestDto(status);
	}
}