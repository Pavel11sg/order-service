package com.example.tasks.orderservice.service;

import com.example.tasks.orderservice.dto.mapper.OrderMapper;
import com.example.tasks.orderservice.dto.request.OrderCreateRequestDto;
import com.example.tasks.orderservice.dto.request.OrderItemRequestDto;
import com.example.tasks.orderservice.dto.request.OrderUpdateRequestDto;
import com.example.tasks.orderservice.dto.response.OrderResponseDto;
import com.example.tasks.orderservice.dto.response.OrderWithUserResponseDto;
import com.example.tasks.orderservice.dto.response.UserResponseDto;
import com.example.tasks.orderservice.exception.InvalidOrderRequestException;
import com.example.tasks.orderservice.exception.InvalidStatusTransitionException;
import com.example.tasks.orderservice.exception.OrderNotFoundException;
import com.example.tasks.orderservice.model.Order;
import com.example.tasks.orderservice.model.OrderItem;
import com.example.tasks.orderservice.model.OrderStatus;
import com.example.tasks.orderservice.proxy.UserServiceClient;
import com.example.tasks.orderservice.repository.OrderRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {
	private final OrderRepository orderRepository;
	private final ItemService itemService;
	private final OrderMapper orderMapper;
	private final Map<OrderStatus, Set<OrderStatus>> allowedTransitions = createTransitionsMap();
	private final UserServiceClient userServiceClient;

	public OrderService(OrderRepository orderRepository, ItemService itemService, OrderMapper orderMapper, UserServiceClient userServiceClient) {
		this.orderRepository = orderRepository;
		this.itemService = itemService;
		this.orderMapper = orderMapper;
		this.userServiceClient = userServiceClient;
	}

	@Transactional
	public OrderWithUserResponseDto createOrder(OrderCreateRequestDto orderRequestDto) {
		validateOrderRequest(orderRequestDto);
		List<OrderItemRequestDto> orderRequestDtoItems = orderRequestDto.getItems();
		validateItemsAvailability(orderRequestDtoItems);
		Order order = createOrderEntity(orderRequestDto);
		updateStockQuantities(orderRequestDtoItems);
		UserResponseDto userResponseDto = userServiceClient.getUser(order.getUserId());
		OrderResponseDto orderResponseDto = orderMapper.toDto(orderRepository.save(order));
		return new OrderWithUserResponseDto(userResponseDto, orderResponseDto);
	}

	@Transactional
	public OrderWithUserResponseDto updateOrder(UUID orderId, OrderUpdateRequestDto requestDto) {
		OrderStatus currentStatus = requestDto.getStatus();
		OrderStatus newStatus = requestDto.getStatus();
		Set<OrderStatus> allowed = allowedTransitions.get(currentStatus);
		if (allowed == null || !allowed.contains(newStatus)) {
			throw new InvalidStatusTransitionException(String.format("Invalid transition from %s to %s", currentStatus, newStatus));
		}
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException(String.format("Order with id: %s not found", orderId)));
		order.setOrderStatus(newStatus);
		UserResponseDto userResponseDto = userServiceClient.getUser(order.getUserId());
		OrderResponseDto orderResponseDto = orderMapper.toDto(orderRepository.save(order));
		return new OrderWithUserResponseDto(userResponseDto, orderResponseDto);
	}

	@Transactional
	public void deleteOrder(UUID orderId) {
		if (!orderRepository.existsById(orderId)) {
			throw new OrderNotFoundException(String.format("Order with id: %s not found", orderId));
		}
		orderRepository.deleteById(orderId);
	}

	@Transactional(readOnly = true)
	public OrderWithUserResponseDto getOrderById(UUID orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException(String.format("Order with id: %s not found", orderId)));
		UserResponseDto userResponseDto = userServiceClient.getUser(order.getUserId());
		OrderResponseDto orderResponseDto = orderMapper.toDto(order);
		return new OrderWithUserResponseDto(userResponseDto, orderResponseDto);
	}

	@Transactional(readOnly = true)
	public List<OrderWithUserResponseDto> getOrdersByIds(List<UUID> uuids) {
		if (uuids == null || uuids.isEmpty()) {
			return Collections.emptyList();
		}
		List<Order> orders = orderRepository.findByOrderIdIn(uuids);
		return combineOrdersWithUsers(orders);
	}

	@Transactional(readOnly = true)
	public List<OrderWithUserResponseDto> getOrdersByStatuses(List<OrderStatus> statuses) {
		if (statuses == null || statuses.isEmpty()) {
			return Collections.emptyList();
		}
		List<Order> orders = orderRepository.findByOrderStatusIn(statuses);
		return combineOrdersWithUsers(orders);
	}

	@NotNull
	private List<OrderWithUserResponseDto> combineOrdersWithUsers(List<Order> orders) {
		if (orders.isEmpty()) {
			return Collections.emptyList();
		}
		Set<UUID> userIds = orders.stream()
				.map(Order::getUserId)
				.collect(Collectors.toSet());
		Map<UUID, UserResponseDto> usersMap = userServiceClient.getUsersByIds(new ArrayList<>(userIds))
				.stream()
				.collect(Collectors.toMap(UserResponseDto::getId, Function.identity()));
		return orders.stream()
				.map(order -> {
					OrderResponseDto orderResponseDto = orderMapper.toDto(order);
					UserResponseDto userResponseDto = usersMap.get(order.getUserId());
					return new OrderWithUserResponseDto(userResponseDto, orderResponseDto);
				})
				.collect(Collectors.toList());
	}

	private void validateOrderRequest(OrderCreateRequestDto orderRequestDto) {
		if (orderRequestDto.getUserId() == null || orderRequestDto.getItems().isEmpty()) {
			throw new InvalidOrderRequestException("User ID is required and items list cannot be empty");
		}
	}

	private void validateItemsAvailability(List<OrderItemRequestDto> items) {
		for (OrderItemRequestDto item : items) {
			if (!itemService.isItemAvailable(item.getItemId(), item.getQuantity())) {
				throw new InvalidOrderRequestException(
						String.format("Not enough stock for item: %s, requested: %d",
								item.getItemId(), item.getQuantity()));
			}
		}
	}

	private Order createOrderEntity(OrderCreateRequestDto orderRequestDto) {
		Order order = orderMapper.toEntity(orderRequestDto);
		List<OrderItem> orderItems = orderRequestDto.getItems().stream()
				.map(itemDto -> {
					OrderItem orderItem = orderMapper.toEntity(itemDto);
					orderItem.setOrder(order);
					return orderItem;
				})
				.collect(Collectors.toList());
		order.setOrderItems(orderItems);
		return order;
	}

	private void updateStockQuantities(List<OrderItemRequestDto> items) {
		for (OrderItemRequestDto item : items) {
			itemService.decreaseStockQuantity(item.getItemId(), item.getQuantity());
		}
	}

	private Map<OrderStatus, Set<OrderStatus>> createTransitionsMap() {
		Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
		transitions.put(OrderStatus.CREATED, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED, OrderStatus.EXPIRED));
		transitions.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED));
		transitions.put(OrderStatus.PAYMENT_PENDING, Set.of(OrderStatus.PAYMENT_RECEIVED, OrderStatus.PAYMENT_FAILED, OrderStatus.CANCELLED));
		transitions.put(OrderStatus.PAYMENT_RECEIVED, Set.of(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED));
		transitions.put(OrderStatus.IN_PROGRESS, Set.of(OrderStatus.DELIVERY, OrderStatus.CANCELLED));
		transitions.put(OrderStatus.DELIVERY, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
		transitions.put(OrderStatus.PAYMENT_FAILED, Set.of(OrderStatus.CANCELLED, OrderStatus.EXPIRED));
		transitions.put(OrderStatus.CANCELLED, Set.of());
		transitions.put(OrderStatus.EXPIRED, Set.of());
		transitions.put(OrderStatus.COMPLETED, Set.of());
		return transitions;
	}
}
