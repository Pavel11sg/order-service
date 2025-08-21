package com.example.tasks.orderservice.dto.mapper;

import com.example.tasks.orderservice.dto.request.OrderCreateRequestDto;
import com.example.tasks.orderservice.dto.request.OrderItemRequestDto;
import com.example.tasks.orderservice.dto.response.OrderItemResponseDto;
import com.example.tasks.orderservice.dto.response.OrderResponseDto;
import com.example.tasks.orderservice.model.Order;
import com.example.tasks.orderservice.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		imports = BigDecimal.class)
public interface OrderMapper {
	@Mapping(target = "orderId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "orderItems", ignore = true)
	@Mapping(target = "totalAmount", ignore = true)
	@Mapping(target = "orderStatus", ignore = true)
	@Mapping(source = "userId", target = "userId")
	Order toEntity(OrderCreateRequestDto orderCreateRequestDto);

	@Mapping(source = "orderId", target = "orderId")
	@Mapping(source = "orderStatus", target = "status")
	@Mapping(source = "totalAmount", target = "totalAmount")
	@Mapping(source = "createdAt", target = "createdAt")
	@Mapping(source = "updatedAt", target = "updatedAt")
	@Mapping(source = "orderItems", target = "items")
	OrderResponseDto toDto(Order order);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "order", ignore = true)
	@Mapping(target = "item", ignore = true)
	@Mapping(target = "price", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	OrderItem toEntity(OrderItemRequestDto orderItemRequestDto);

	@Mapping(source = "item.itemId", target = "itemId")
	@Mapping(source = "item.name", target = "itemName")
	@Mapping(source = "quantity", target = "quantity")
	@Mapping(source = "price", target = "price")
	@Mapping(target = "subtotal", expression = "java(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))")
	OrderItemResponseDto toDto(OrderItem orderItem);
}
