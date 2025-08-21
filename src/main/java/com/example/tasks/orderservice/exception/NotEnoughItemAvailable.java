package com.example.tasks.orderservice.exception;

public class NotEnoughItemAvailable extends RuntimeException {
	public NotEnoughItemAvailable(String message) {
		super(message);
	}
}
