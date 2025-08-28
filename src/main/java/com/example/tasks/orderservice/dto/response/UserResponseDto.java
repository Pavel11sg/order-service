package com.example.tasks.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponseDto {
	private UUID id;
	private String name;
	private String surname;
	private String maskedEmail;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthDate;
	private List<CardResponseDto> cards;
}
