package com.example.tasks.orderservice.kafka.consumer;


import com.example.tasks.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.example.tasks.dto.PaymentCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentCreatedEventConsumer {

    private final OrderService orderService;

    public PaymentCreatedEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = "${kafka.topics.payment-created}",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreatedEvent(PaymentCreatedEvent paymentCreatedEvent) {
        log.info("Received PaymentCreatedEvent for order: {}", paymentCreatedEvent.getOrderId());
        try {
            orderService.processPaymentCreatedEvent(paymentCreatedEvent);
        } catch (Exception e) {
            log.error("Error handling PaymentCreatedEvent for order: {}",
                    paymentCreatedEvent.getOrderId(), e);
        }
    }
}
