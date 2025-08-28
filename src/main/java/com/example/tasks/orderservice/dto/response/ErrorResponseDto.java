package com.example.tasks.orderservice.dto.response;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorResponseDto {
	private LocalDateTime timestamp;
	private int status;
	private String error;
	private String message;
	private String path;

	public ErrorResponseDto(int status, String error, String message, String path) {
		this.timestamp = LocalDateTime.now();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}

}
