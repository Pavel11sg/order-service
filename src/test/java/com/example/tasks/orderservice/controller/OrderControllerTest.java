package com.example.tasks.orderservice.controller;

import com.example.tasks.orderservice.dto.request.OrderCreateRequestDto;
import com.example.tasks.orderservice.dto.request.OrderUpdateRequestDto;
import com.example.tasks.orderservice.dto.response.OrderWithUserResponseDto;
import com.example.tasks.orderservice.exception.OrderNotFoundException;
import com.example.tasks.orderservice.model.OrderStatus;
import com.example.tasks.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.example.tasks.orderservice.controller.OrderControllerTestData.ORDER_ID_1;
import static com.example.tasks.orderservice.controller.OrderControllerTestData.USER_ID_1;
import static com.example.tasks.orderservice.controller.OrderControllerTestData.createOrderIdList;
import static com.example.tasks.orderservice.controller.OrderControllerTestData.createOrderList;
import static com.example.tasks.orderservice.controller.OrderControllerTestData.createOrderRequest;
import static com.example.tasks.orderservice.controller.OrderControllerTestData.createOrderRequestWithSingleItem;
import static com.example.tasks.orderservice.controller.OrderControllerTestData.createOrderUpdateRequest;
import static com.example.tasks.orderservice.controller.OrderControllerTestData.createOrderWithStatus;
import static com.example.tasks.orderservice.controller.OrderControllerTestData.createOrderWithUserResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OrderService orderService;

	@Test
	void createOrder_ValidRequest_ReturnsCreated() throws Exception {
		// Arrange
		OrderCreateRequestDto request = createOrderRequest();
		OrderWithUserResponseDto response = createOrderWithUserResponse();
		when(orderService.createOrder(any(OrderCreateRequestDto.class))).thenReturn(response);
		// Act & Assert
		mockMvc.perform(post("/orderservice/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.order.orderId").exists())
				.andExpect(jsonPath("$.user.id").value(USER_ID_1.toString()));
		verify(orderService).createOrder(any(OrderCreateRequestDto.class));
	}

	@Test
	void getOrderById_ExistingId_ReturnsOrder() throws Exception {
		// Arrange
		OrderWithUserResponseDto response = createOrderWithUserResponse();
		when(orderService.getOrderById(ORDER_ID_1)).thenReturn(response);
		// Act & Assert
		mockMvc.perform(get("/orderservice/orders/{orderId}", ORDER_ID_1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.order.orderId").value(ORDER_ID_1.toString()))
				.andExpect(jsonPath("$.order.status").value("CREATED"));
		verify(orderService).getOrderById(ORDER_ID_1);
	}

	@Test
	void updateOrder_ValidRequest_ReturnsUpdatedOrder() throws Exception {
		// Arrange
		OrderUpdateRequestDto request = createOrderUpdateRequest(OrderStatus.IN_PROGRESS);
		OrderWithUserResponseDto response = createOrderWithStatus(OrderStatus.IN_PROGRESS);
		when(orderService.updateOrder(eq(ORDER_ID_1), any(OrderUpdateRequestDto.class)))
				.thenReturn(response);
		// Act & Assert
		mockMvc.perform(put("/orderservice/orders/{orderId}", ORDER_ID_1)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.order.status").value("IN_PROGRESS"));
		verify(orderService).updateOrder(eq(ORDER_ID_1), any(OrderUpdateRequestDto.class));
	}

	@Test
	void getOrdersByIds_ValidIds_ReturnsOrders() throws Exception {
		// Arrange
		List<OrderWithUserResponseDto> responses = createOrderList(2);
		List<UUID> orderIds = createOrderIdList();
		when(orderService.getOrdersByIds(anyList())).thenReturn(responses);
		// Act & Assert
		mockMvc.perform(get("/orderservice/orders/batch/{ids}", String.join(",", orderIds.stream()
						.map(UUID::toString)
						.toArray(String[]::new))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(2));
		verify(orderService).getOrdersByIds(anyList());
	}

	@Test
	void getOrdersByStatuses_ValidStatuses_ReturnsOrders() throws Exception {
		// Arrange
		List<OrderWithUserResponseDto> responses = createOrderList(2);
		when(orderService.getOrdersByStatuses(anyList())).thenReturn(responses);
		// Act & Assert
		mockMvc.perform(get("/orderservice/orders")
						.param("status", "CREATED", "IN_PROGRESS"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].order.status").exists());
		verify(orderService).getOrdersByStatuses(anyList());
	}

	@Test
	void deleteOrder_ExistingId_ReturnsNoContent() throws Exception {
		// Arrange
		doNothing().when(orderService).deleteOrder(ORDER_ID_1);
		// Act & Assert
		mockMvc.perform(delete("/orderservice/orders/{orderId}", ORDER_ID_1))
				.andExpect(status().isNoContent());
		verify(orderService).deleteOrder(ORDER_ID_1);
	}

	@Test
	void createOrder_InvalidRequest_ReturnsBadRequest() throws Exception {
		// Arrange
		OrderCreateRequestDto invalidRequest = new OrderCreateRequestDto(
				null,
				List.of()
		);
		// Act & Assert
		mockMvc.perform(post("/orderservice/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists())
				.andExpect(jsonPath("$.error").exists())
				.andExpect(jsonPath("$.path").exists());
		verify(orderService, never()).createOrder(any());
	}

	@Test
	void getOrderById_NonExistingId_ReturnsNotFound() throws Exception {
		// Arrange
		UUID nonExistingId = UUID.randomUUID();
		when(orderService.getOrderById(nonExistingId))
				.thenThrow(new OrderNotFoundException(String.format("Order with id: %s not found", nonExistingId)));
		// Act & Assert
		mockMvc.perform(get("/orderservice/orders/{orderId}", nonExistingId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").exists())
				.andExpect(jsonPath("$.error").exists())
				.andExpect(jsonPath("$.path").exists());
		verify(orderService).getOrderById(nonExistingId);
	}

	@Test
	void createOrder_SingleItem_ReturnsCreated() throws Exception {
		// Arrange
		OrderCreateRequestDto request = createOrderRequestWithSingleItem();
		OrderWithUserResponseDto response = createOrderWithUserResponse();
		when(orderService.createOrder(any(OrderCreateRequestDto.class))).thenReturn(response);
		// Act & Assert
		mockMvc.perform(post("/orderservice/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.order.userId").value(request.getUserId().toString()))
				.andExpect(jsonPath("$.order.items.length()").value(2));;
		verify(orderService).createOrder(any(OrderCreateRequestDto.class));
	}
}