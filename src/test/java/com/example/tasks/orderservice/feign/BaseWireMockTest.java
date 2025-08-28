package com.example.tasks.orderservice.feign;

import com.example.tasks.orderservice.proxy.UserServiceClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


public abstract class BaseWireMockTest {

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort())
			.build();

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("user-service.url", wireMock::baseUrl);
	}
}