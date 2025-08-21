package com.example.tasks.orderservice.exception;

public class InvalidOrderRequestException extends RuntimeException {
	public InvalidOrderRequestException(String message) {
		super(message);
	}
}
