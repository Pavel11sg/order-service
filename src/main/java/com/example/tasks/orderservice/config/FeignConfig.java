package com.example.tasks.orderservice.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.example.tasks.orderservice.proxy")
public class FeignConfig {
}
