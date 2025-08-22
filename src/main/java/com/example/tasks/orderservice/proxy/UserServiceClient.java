package com.example.tasks.orderservice.proxy;

import com.example.tasks.orderservice.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserServiceClient {
	@GetMapping("userservice/users/{userId}")
	UserResponseDto getUser(@PathVariable("userId") UUID userId);

	@GetMapping("userservice/users/batch")
	List<UserResponseDto> getUsersByIds(@RequestParam("id") List<UUID> ids);
}
