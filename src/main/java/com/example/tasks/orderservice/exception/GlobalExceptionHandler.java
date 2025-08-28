package com.example.tasks.orderservice.exception;

import com.example.tasks.orderservice.dto.response.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler({
			InvalidOrderRequestException.class,
			InvalidStatusTransitionException.class,
			ItemNotFoundException.class,
			NotEnoughItemAvailable.class,
			OrderNotFoundException.class,
			UserNotFoundException.class
	})
	public ResponseEntity<ErrorResponseDto> handleCustomExceptions(RuntimeException ex, HttpServletRequest request) {
		HttpStatus status = determineHttpStatus(ex);
		ErrorResponseDto errorResponse = new ErrorResponseDto(
				status.value(),
				status.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI()
		);
		return new ResponseEntity<>(errorResponse, status);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
			MethodArgumentNotValidException ex, HttpServletRequest request) {

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error ->
				errors.put(error.getField(), error.getDefaultMessage())
		);

		ErrorResponseDto errorResponse = new ErrorResponseDto(
				HttpStatus.BAD_REQUEST.value(),
				"Validation Failed",
				errors.toString(),
				request.getRequestURI()
		);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
			IllegalArgumentException ex, HttpServletRequest request) {

		ErrorResponseDto errorResponse = new ErrorResponseDto(
				HttpStatus.BAD_REQUEST.value(),
				"Bad Request",
				ex.getMessage(),
				request.getRequestURI()
		);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponseDto> handleIllegalStateException(
			IllegalStateException ex, HttpServletRequest request) {

		ErrorResponseDto errorResponse = new ErrorResponseDto(
				HttpStatus.CONFLICT.value(),
				"Conflict",
				ex.getMessage(),
				request.getRequestURI()
		);
		return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDto> handleGlobalException(
			Exception ex, HttpServletRequest request) {

		ErrorResponseDto errorResponse = new ErrorResponseDto(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error",
				"An unexpected error occurred",
				request.getRequestURI()
		);
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private HttpStatus determineHttpStatus(RuntimeException ex) {
		if (ex instanceof InvalidOrderRequestException ||
				ex instanceof InvalidStatusTransitionException) {
			return HttpStatus.BAD_REQUEST;
		} else if (ex instanceof ItemNotFoundException ||
				ex instanceof OrderNotFoundException ||
				ex instanceof UserNotFoundException) {
			return HttpStatus.NOT_FOUND;
		} else if (ex instanceof NotEnoughItemAvailable) {
			return HttpStatus.CONFLICT;
		} else {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}
}
