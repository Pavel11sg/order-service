package com.example.tasks.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemRequestDto {
	@NotBlank(message = "Name is required")
	private String name;

	@NotNull(message = "Price is required")
	@Positive(message = "Price must be greater than 0")
	private BigDecimal price;

	@NotBlank(message = "Description can not be empty, if there is no description, please, put N/A instead")
	private String description;

	@NotNull
	@Positive(message = "Stock quantity should be positive")
	private Integer stockQuantity;
}
