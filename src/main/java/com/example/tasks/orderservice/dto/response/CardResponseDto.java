package com.example.tasks.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CardResponseDto {
	private UUID id;
	private String lastFourDigits;
	private String maskedHolder;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate expirationDate;
}
