package com.example.tasks.orderservice.unit;

import com.example.tasks.orderservice.dto.mapper.OrderMapper;
import com.example.tasks.orderservice.dto.request.OrderCreateRequestDto;
import com.example.tasks.orderservice.dto.request.OrderUpdateRequestDto;
import com.example.tasks.orderservice.dto.response.OrderResponseDto;
import com.example.tasks.orderservice.dto.response.OrderWithUserResponseDto;
import com.example.tasks.orderservice.exception.InvalidOrderRequestException;
import com.example.tasks.orderservice.exception.InvalidStatusTransitionException;
import com.example.tasks.orderservice.exception.OrderNotFoundException;
import com.example.tasks.orderservice.exception.UserNotFoundException;
import com.example.tasks.orderservice.model.Item;
import com.example.tasks.orderservice.model.Order;
import com.example.tasks.orderservice.model.OrderStatus;
import com.example.tasks.orderservice.proxy.UserServiceClient;
import com.example.tasks.orderservice.repository.OrderRepository;
import com.example.tasks.orderservice.service.ItemService;
import com.example.tasks.orderservice.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.tasks.orderservice.unit.OrderTestData.ITEM_ID_1;
import static com.example.tasks.orderservice.unit.OrderTestData.ITEM_ID_2;
import static com.example.tasks.orderservice.unit.OrderTestData.ORDER_CREATE_REQUEST;
import static com.example.tasks.orderservice.unit.OrderTestData.ORDER_ID;
import static com.example.tasks.orderservice.unit.OrderTestData.ORDER_UPDATE_REQUEST;
import static com.example.tasks.orderservice.unit.OrderTestData.USER_ID;
import static com.example.tasks.orderservice.unit.OrderTestData.USER_RESPONSE;
import static com.example.tasks.orderservice.unit.OrderTestData.createDefaultOrder;
import static com.example.tasks.orderservice.unit.OrderTestData.createDefaultOrderResponseDto;
import static com.example.tasks.orderservice.unit.OrderTestData.createOrder;
import static com.example.tasks.orderservice.unit.OrderTestData.createOrderRequestWithEmptyItems;
import static com.example.tasks.orderservice.unit.OrderTestData.createOrderRequestWithNullUserId;
import static com.example.tasks.orderservice.unit.OrderTestData.createOrderResponseDto;
import static com.example.tasks.orderservice.unit.OrderTestData.createOrderUpdateRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Unit Tests")
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ItemService itemService;

	@Mock
	private OrderMapper orderMapper;

	@Mock
	private UserServiceClient userServiceClient;

	@InjectMocks
	private OrderService orderService;

	@Nested
	@DisplayName("Create Order Tests")
	class CreateOrderTests {

		@Test
		@DisplayName("Should create order successfully with valid request")
		void createOrder_withValidRequest_shouldReturnOrderWithUser() {
			// Arrange
			Order savedOrder = createDefaultOrder();
			OrderResponseDto orderResponse = createDefaultOrderResponseDto();
			when(userServiceClient.getUser(USER_ID)).thenReturn(USER_RESPONSE);
			when(itemService.isItemAvailable(any(UUID.class), anyInt())).thenReturn(true);
			// Заглушки для getItemById
			Item item1 = OrderTestData.createTestItem("iPhone 16", BigDecimal.valueOf(3900));
			Item item2 = OrderTestData.createTestItem("HP ProBook", BigDecimal.valueOf(4100));
			when(itemService.getItemById(ITEM_ID_1)).thenReturn(item1);
			when(itemService.getItemById(ITEM_ID_2)).thenReturn(item2);
			when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
			when(orderMapper.toDto(savedOrder)).thenReturn(orderResponse);
			// Act
			OrderWithUserResponseDto result = orderService.createOrder(ORDER_CREATE_REQUEST);
			// Assert
			assertThat(result).isNotNull();
			assertThat(result.getOrder().getOrderId()).isEqualTo(ORDER_ID);
			assertThat(result.getUser().getId()).isEqualTo(USER_ID);
			// Verify
			verify(userServiceClient).getUser(USER_ID);
			verify(itemService, times(2)).isItemAvailable(any(UUID.class), anyInt());
			verify(itemService, times(2)).decreaseStockQuantity(any(UUID.class), anyInt());
			verify(itemService).getItemById(ITEM_ID_1);
			verify(itemService).getItemById(ITEM_ID_2);
			verify(orderRepository).save(any(Order.class));
			verify(orderMapper).toDto(savedOrder);
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when user not found")
		void createOrder_withNonExistentUser_shouldThrowUserNotFoundException() {
			// Arrange
			when(userServiceClient.getUser(USER_ID)).thenReturn(null);
			// Act & Assert
			assertThatThrownBy(() -> orderService.createOrder(ORDER_CREATE_REQUEST))
					.isInstanceOf(UserNotFoundException.class)
					.hasMessageContaining("User not found, id: " + USER_ID);
			// Verify
			verify(orderRepository, never()).save(any());
			verify(userServiceClient).getUser(USER_ID);
			verify(itemService, never()).isItemAvailable(any(UUID.class), anyInt());
			verify(itemService, never()).getItemById(any(UUID.class));
			verify(itemService, never()).decreaseStockQuantity(any(UUID.class), anyInt());
		}

		@Test
		@DisplayName("Should throw InvalidOrderRequestException when item not available")
		void createOrder_withUnavailableItem_shouldThrowInvalidOrderRequestException() {
			// Arrange
			when(userServiceClient.getUser(USER_ID)).thenReturn(USER_RESPONSE);
			when(itemService.isItemAvailable(ITEM_ID_1, 1)).thenReturn(false);
			// Act & Assert
			assertThatThrownBy(() -> orderService.createOrder(ORDER_CREATE_REQUEST))
					.isInstanceOf(InvalidOrderRequestException.class)
					.hasMessageContaining("Not enough stock");
			// Verify
			verify(orderRepository, never()).save(any());
			verify(userServiceClient).getUser(USER_ID);
			verify(itemService).isItemAvailable(ITEM_ID_1, 1);
			verify(itemService, never()).isItemAvailable(ITEM_ID_2, 2);
		}

		@Test
		@DisplayName("Should throw InvalidOrderRequestException when userId is null")
		void createOrder_withNullUserId_shouldThrowInvalidOrderRequestException() {
			// Arrange
			OrderCreateRequestDto invalidRequest = createOrderRequestWithNullUserId();
			// Act & Assert
			assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
					.isInstanceOf(InvalidOrderRequestException.class)
					.hasMessageContaining("User ID is required");
			// Verify
			verify(orderRepository, never()).save(any());
			verify(userServiceClient, never()).getUser(any());
			verify(itemService, never()).isItemAvailable(any(), anyInt());
		}

		@Test
		@DisplayName("Should throw InvalidOrderRequestException when items list is empty")
		void createOrder_withEmptyItems_shouldThrowInvalidOrderRequestException() {
			// Arrange
			OrderCreateRequestDto invalidRequest = createOrderRequestWithEmptyItems();
			// Act & Assert
			assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
					.isInstanceOf(InvalidOrderRequestException.class)
					.hasMessageContaining("items list cannot be empty");
			// Verify
			verify(orderRepository, never()).save(any());
			verify(userServiceClient, never()).getUser(any());
			verify(itemService, never()).isItemAvailable(any(), anyInt());
		}
	}

	@Nested
	@DisplayName("Get Order Tests")
	class GetOrderTests {

		@Test
		@DisplayName("Should return order by id successfully")
		void getOrderById_withValidId_shouldReturnOrder() {
			// Arrange
			Order order = createDefaultOrder();
			OrderResponseDto orderResponse = createDefaultOrderResponseDto();
			when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
			when(userServiceClient.getUser(USER_ID)).thenReturn(USER_RESPONSE);
			when(orderMapper.toDto(order)).thenReturn(orderResponse);
			// Act
			OrderWithUserResponseDto result = orderService.getOrderById(ORDER_ID);
			// Assert
			assertThat(result).isNotNull();
			assertThat(result.getOrder().getOrderId()).isEqualTo(ORDER_ID);
			assertThat(result.getUser().getId()).isEqualTo(USER_ID);
			// Verify
			verify(orderRepository).findById(ORDER_ID);
			verify(userServiceClient).getUser(USER_ID);
			verify(orderMapper).toDto(order);
		}

		@Test
		@DisplayName("Should throw OrderNotFoundException when order not found")
		void getOrderById_withNonExistentId_shouldThrowOrderNotFoundException() {
			// Arrange
			when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());
			// Act & Assert
			assertThatThrownBy(() -> orderService.getOrderById(ORDER_ID))
					.isInstanceOf(OrderNotFoundException.class)
					.hasMessageContaining("not found");
			// Verify
			verify(userServiceClient, never()).getUser(any());
		}
	}

	@Nested
	@DisplayName("Update Order Tests")
	class UpdateOrderTests {

		@Test
		@DisplayName("Should update order status successfully")
		void updateOrder_withValidStatus_shouldUpdateOrder() {
			// Arrange
			Order order = createOrder(ORDER_ID, USER_ID, OrderStatus.CREATED, BigDecimal.valueOf(12100));
			OrderResponseDto orderResponse = createOrderResponseDto(ORDER_ID, USER_ID, OrderStatus.CONFIRMED);
			when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
			when(orderRepository.save(order)).thenReturn(order);
			when(userServiceClient.getUser(USER_ID)).thenReturn(USER_RESPONSE);
			when(orderMapper.toDto(order)).thenReturn(orderResponse);
			// Act
			OrderWithUserResponseDto result = orderService.updateOrder(ORDER_ID, ORDER_UPDATE_REQUEST);
			// Assert
			assertThat(result).isNotNull();
			assertThat(result.getOrder().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
			// Verify
			verify(orderRepository).findById(ORDER_ID);
			verify(orderRepository).save(order);
			verify(userServiceClient).getUser(USER_ID);
		}

		@Test
		@DisplayName("Should throw InvalidStatusTransitionException for invalid status transition")
		void updateOrder_withInvalidStatusTransition_shouldThrowInvalidStatusTransitionException() {
			// Arrange
			Order order = createOrder(ORDER_ID, USER_ID, OrderStatus.CREATED, BigDecimal.valueOf(12100));
			OrderUpdateRequestDto invalidRequest = createOrderUpdateRequest(OrderStatus.COMPLETED);
			when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
			// Act & Assert
			assertThatThrownBy(() -> orderService.updateOrder(ORDER_ID, invalidRequest))
					.isInstanceOf(InvalidStatusTransitionException.class)
					.hasMessageContaining("Invalid transition");
			// Verify
			verify(orderRepository, never()).save(any());
			verify(userServiceClient, never()).getUser(any());
		}
	}

	@Nested
	@DisplayName("Delete Order Tests")
	class DeleteOrderTests {

		@Test
		@DisplayName("Should delete order successfully")
		void deleteOrder_withValidId_shouldDeleteOrder() {
			// Arrange
			when(orderRepository.existsById(ORDER_ID)).thenReturn(true);
			// Act
			orderService.deleteOrder(ORDER_ID);
			// Assert & Verify
			verify(orderRepository).existsById(ORDER_ID);
			verify(orderRepository).deleteById(ORDER_ID);
		}

		@Test
		@DisplayName("Should throw OrderNotFoundException when order not found for deletion")
		void deleteOrder_withNonExistentId_shouldThrowOrderNotFoundException() {
			// Arrange
			when(orderRepository.existsById(ORDER_ID)).thenReturn(false);
			// Act & Assert
			assertThatThrownBy(() -> orderService.deleteOrder(ORDER_ID))
					.isInstanceOf(OrderNotFoundException.class)
					.hasMessageContaining("not found");
			// Verify
			verify(orderRepository, never()).deleteById(any());
		}
	}

	@Nested
	@DisplayName("Batch Operations Tests")
	class BatchOperationsTests {

		@Test
		@DisplayName("Should return orders by ids successfully")
		void getOrdersByIds_withValidIds_shouldReturnOrders() {
			// Arrange
			List<UUID> orderIds = List.of(ORDER_ID);
			Order order = createDefaultOrder();
			OrderResponseDto orderResponse = createDefaultOrderResponseDto();
			when(orderRepository.findByOrderIdIn(orderIds)).thenReturn(List.of(order));
			when(userServiceClient.getUsersByIds(anyList())).thenReturn(List.of(USER_RESPONSE));
			when(orderMapper.toDto(order)).thenReturn(orderResponse);
			// Act
			List<OrderWithUserResponseDto> result = orderService.getOrdersByIds(orderIds);
			// Assert
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getOrder().getOrderId()).isEqualTo(ORDER_ID);
			// Verify
			verify(orderRepository).findByOrderIdIn(orderIds);
			verify(userServiceClient).getUsersByIds(List.of(USER_ID));
		}

		@Test
		@DisplayName("Should return empty list for empty ids")
		void getOrdersByIds_withEmptyIds_shouldReturnEmptyList() {
			// Act
			List<OrderWithUserResponseDto> result = orderService.getOrdersByIds(List.of());
			// Assert
			assertThat(result).isEmpty();
			// Verify
			verify(orderRepository, never()).findByOrderIdIn(any());
		}
	}
}