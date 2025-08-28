package com.example.tasks.orderservice.integration;

import com.example.tasks.orderservice.TestcontainersConfiguration;
import com.example.tasks.orderservice.dto.request.OrderCreateRequestDto;
import com.example.tasks.orderservice.dto.request.OrderUpdateRequestDto;
import com.example.tasks.orderservice.dto.response.OrderWithUserResponseDto;
import com.example.tasks.orderservice.exception.InvalidOrderRequestException;
import com.example.tasks.orderservice.exception.UserNotFoundException;
import com.example.tasks.orderservice.model.Item;
import com.example.tasks.orderservice.model.Order;
import com.example.tasks.orderservice.model.OrderStatus;
import com.example.tasks.orderservice.proxy.UserServiceClient;
import com.example.tasks.orderservice.repository.ItemRepository;
import com.example.tasks.orderservice.repository.OrderRepository;
import com.example.tasks.orderservice.service.OrderService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class OrderServiceIntegrationTest {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ItemRepository itemRepository;

	@MockitoBean
	private UserServiceClient userServiceClient;

	@BeforeEach
	void setUp() {
		orderRepository.deleteAll();
		itemRepository.deleteAll();
	}

	@Test
	@Transactional
	void createOrder_WithValidData_ShouldCreateOrderAndDecreaseStock() {
		//Arrange
		Item newItem = OrderServiceTestData.createItemWithStock(10);
		Item item = itemRepository.save(newItem);
		when(userServiceClient.getUser(OrderServiceTestData.USER_ID_1))
				.thenReturn(OrderServiceTestData.createDefaultUserResponse());
		OrderCreateRequestDto request = OrderServiceTestData.createDefaultOrderRequest(item.getItemId());
		//Act
		OrderWithUserResponseDto result = orderService.createOrder(request);
		//Assert
		assertThat(result).isNotNull();
		assertThat(result.getOrder().getOrderId()).isNotNull();
		assertThat(result.getOrder().getStatus()).isEqualTo(OrderStatus.CREATED);
		assertThat(result.getUser().getId()).isEqualTo(OrderServiceTestData.USER_ID_1);
		assertThat(result.getOrder().getItems()).isNotEmpty();
		Item updatedItem = itemRepository.findById(item.getItemId()).orElseThrow();
		Assertions.assertThat(updatedItem.getStockQuantity()).isEqualTo(7);
		assertThat(orderRepository.existsById(result.getOrder().getOrderId())).isTrue();
	}

	@Test
	void createOrder_WithInsufficientStock_ShouldThrowExceptionAndRollbackTransaction() {
		// Arrange
		Item item = itemRepository.save(OrderServiceTestData.createItemWithStock(2));
		when(userServiceClient.getUser(OrderServiceTestData.USER_ID_1))
				.thenReturn(OrderServiceTestData.createDefaultUserResponse());
		OrderCreateRequestDto request = OrderServiceTestData.createOrderRequestWithInsufficientStock(item.getItemId());
		// Act & Assert
		assertThatThrownBy(() -> orderService.createOrder(request))
				.isInstanceOf(InvalidOrderRequestException.class)
				.hasMessageContaining("Not enough stock");
		Item unchangedItem = itemRepository.findById(item.getItemId()).orElseThrow();
		assertThat(unchangedItem.getStockQuantity()).isEqualTo(item.getStockQuantity());
		assertThat(orderRepository.count()).isZero();
	}

	@Test
	void createOrder_WithMultipleItems_ShouldProcessAllItemsCorrectly() {
		// Arrange
		Item item1 = itemRepository.save(OrderServiceTestData.createItemWithStock(10));
		Item item2 = itemRepository.save(OrderServiceTestData.createItemWithStock(5));
		when(userServiceClient.getUser(OrderServiceTestData.USER_ID_1))
				.thenReturn(OrderServiceTestData.createDefaultUserResponse());
		OrderCreateRequestDto request = OrderServiceTestData.createOrderRequestWithMultipleItems(
				OrderServiceTestData.USER_ID_1,
				item1.getItemId(), 2,
				item2.getItemId(), 3
		);
		// Act
		OrderWithUserResponseDto result = orderService.createOrder(request);
		// Assert
		assertThat(result.getOrder().getItems()).hasSize(2);
		result.getOrder().getItems().forEach(item -> {
			assertThat(item.getItemId()).isNotNull();
			assertThat(item.getQuantity()).isPositive();
			assertThat(item.getPrice()).isPositive();
		});
		Item updatedItem1 = itemRepository.findById(item1.getItemId()).orElseThrow();
		Item updatedItem2 = itemRepository.findById(item2.getItemId()).orElseThrow();
		assertThat(updatedItem1.getStockQuantity()).isEqualTo(8);
		assertThat(updatedItem2.getStockQuantity()).isEqualTo(2);
		BigDecimal expectedTotal = item1.getPrice().multiply(BigDecimal.valueOf(2))
				.add(item2.getPrice().multiply(BigDecimal.valueOf(3)));
		assertThat(result.getOrder().getTotalAmount()).isEqualByComparingTo(expectedTotal);
	}

	@Test
	void createOrder_WhenUserNotFound_ShouldThrowUserNotFoundException() {
		// Arrange
		Item item = itemRepository.save(OrderServiceTestData.createDefaultItem());
		when(userServiceClient.getUser(OrderServiceTestData.USER_ID_1)).thenReturn(null);
		OrderCreateRequestDto request = OrderServiceTestData.createDefaultOrderRequest(item.getItemId());
		// Act & Assert
		assertThatThrownBy(() -> orderService.createOrder(request))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessageContaining("User not found");
		Item unchangedItem = itemRepository.findById(item.getItemId()).orElseThrow();
		assertThat(unchangedItem.getStockQuantity()).isEqualTo(10);
		assertThat(orderRepository.count()).isZero();
	}

	@Test
	void createOrder_WithNullUserId_ShouldThrowInvalidOrderRequestException() {
		// Arrange
		Item item = itemRepository.save(OrderServiceTestData.createDefaultItem());
		OrderCreateRequestDto request = OrderServiceTestData.createOrderRequestWithNullUserId(item.getItemId());
		// Act & Assert
		assertThatThrownBy(() -> orderService.createOrder(request))
				.isInstanceOf(InvalidOrderRequestException.class)
				.hasMessageContaining("User ID is required");
		assertThat(orderRepository.count()).isZero();
	}

	@Test
	void createOrder_WithEmptyItems_ShouldThrowInvalidOrderRequestException() {
		// Arrange
		OrderCreateRequestDto request = OrderServiceTestData.createOrderRequestWithEmptyItems(OrderServiceTestData.USER_ID_1);
		// Act & Assert
		assertThatThrownBy(() -> orderService.createOrder(request))
				.isInstanceOf(InvalidOrderRequestException.class)
				.hasMessageContaining("items list cannot be empty");
		assertThat(orderRepository.count()).isZero();
	}

	@Test
	void updateOrder_WithValidStatusTransition_ShouldUpdateOrderStatus() {
		// Arrange
		Item item = itemRepository.save(OrderServiceTestData.createDefaultItem());
		when(userServiceClient.getUser(OrderServiceTestData.USER_ID_1))
				.thenReturn(OrderServiceTestData.createDefaultUserResponse());
		OrderCreateRequestDto createRequest = OrderServiceTestData.createDefaultOrderRequest(item.getItemId());
		OrderWithUserResponseDto createdOrder = orderService.createOrder(createRequest);
		when(userServiceClient.getUser(OrderServiceTestData.USER_ID_1))
				.thenReturn(OrderServiceTestData.createDefaultUserResponse());
		// Act
		OrderWithUserResponseDto updateResult = orderService.updateOrder(
				createdOrder.getOrder().getOrderId(),
				new OrderUpdateRequestDto(OrderStatus.CONFIRMED)
		);
		// Assert
		assertThat(updateResult.getOrder().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
		Order updatedOrder = orderRepository.findById(createdOrder.getOrder().getOrderId()).orElseThrow();
		assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
	}
}