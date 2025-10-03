package com.example.tasks.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderCreateWithPaymentRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;
    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequestDto> items;
    @NotBlank(message = "Currency is required")
    private String currency;
    @NotBlank(message = "Payment method token is required for payment orders")
    private String paymentMethodToken;
}
