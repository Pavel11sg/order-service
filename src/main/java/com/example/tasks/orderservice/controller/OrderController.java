package com.example.tasks.orderservice.controller;

import com.example.tasks.orderservice.dto.request.OrderCreateRequestDto;
import com.example.tasks.orderservice.dto.request.OrderUpdateRequestDto;
import com.example.tasks.orderservice.dto.response.OrderWithUserResponseDto;
import com.example.tasks.orderservice.model.OrderStatus;
import com.example.tasks.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orderservice/orders")
public class OrderController {
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	public ResponseEntity<OrderWithUserResponseDto> createOrder(@Valid @RequestBody OrderCreateRequestDto requestDto) {
		return new ResponseEntity<>(orderService.createOrder(requestDto), HttpStatus.CREATED);
	}

	@PutMapping("/{orderId}")
	public ResponseEntity<OrderWithUserResponseDto> updateOrder(@PathVariable(name = "orderId") UUID orderId,@Valid @RequestBody OrderUpdateRequestDto requestDto) {
		return ResponseEntity.ok(orderService.updateOrder(orderId, requestDto));
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderWithUserResponseDto> getOrderById(@PathVariable(name = "orderId") UUID orderId) {
		return ResponseEntity.ok(orderService.getOrderById(orderId));
	}

	@GetMapping("/batch/{ids}")
	public ResponseEntity<List<OrderWithUserResponseDto>> getOrdersByIds(@PathVariable(name = "ids") List<UUID> ids) {
		return ResponseEntity.ok(orderService.getOrdersByIds(ids));
	}

	@GetMapping()
	public ResponseEntity<List<OrderWithUserResponseDto>> getOrdersByStatuses(@RequestParam(name = "status") List<OrderStatus> statuses) {
		return ResponseEntity.ok(orderService.getOrdersByStatuses(statuses));
	}

	@DeleteMapping("/{orderId}")
	public ResponseEntity<Void> deleteOrder(@PathVariable(name = "orderId") UUID orderId) {
		orderService.deleteOrder(orderId);
		return ResponseEntity.noContent().build();
	}
}
