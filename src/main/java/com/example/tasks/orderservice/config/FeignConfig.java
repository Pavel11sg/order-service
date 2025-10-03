package com.example.tasks.orderservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

@Configuration
@EnableFeignClients(basePackages = "com.example.tasks.orderservice.proxy")
public class FeignConfig {
    @Bean
    public RequestInterceptor headersForwardingInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();

            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();

                    if (shouldForwardHeader(headerName)) {
                        String headerValue = request.getHeader(headerName);
                        if (headerValue != null) {
                            requestTemplate.header(headerName, headerValue);
                        }
                    }
                }
            }
        };
    }

    private boolean shouldForwardHeader(String headerName) {
        return headerName.startsWith("X-") ||
                "Authorization".equalsIgnoreCase(headerName) ||
                "X-User-ID".equalsIgnoreCase(headerName) ||
                "X-Internal-Secret".equalsIgnoreCase(headerName);
    }
}
