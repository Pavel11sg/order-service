package com.example.tasks.orderservice.dto.mapper;

import com.example.tasks.orderservice.dto.request.ItemRequestDto;
import com.example.tasks.orderservice.dto.response.ItemResponseDto;
import com.example.tasks.orderservice.model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ItemMapper {
	@Mapping(target = "itemId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "orderItems", ignore = true)
	Item toEntity(ItemRequestDto itemRequestDto);

	@Mapping(source = "itemId", target = "itemId")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "price", target = "price")
	@Mapping(source = "description", target = "description")
	@Mapping(source = "createdAt", target = "createdAt")
	@Mapping(source = "updatedAt", target = "updatedAt")
	ItemResponseDto toDto(Item item);

	@Mapping(target = "itemId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "orderItems", ignore = true)
	void updateItemFromDto(ItemRequestDto itemRequestDto, @MappingTarget Item item);
}