package com.example.tasks.orderservice.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.tasks.config.KafkaSharedConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(
                KafkaSharedConfig.producerConfig(bootstrapServers)
        );
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = KafkaSharedConfig.consumerConfig(
                bootstrapServers, "order-service-group"
        );
        return new DefaultKafkaConsumerFactory<>(props);
    }


    @Value("${kafka.topics.order-created}")
    private String createOrderTopic;

    @Value("${kafka.topics.payment-created}")
    private String createPaymentTopic;

//    @Bean
//    public ProducerFactory<String, Object> producerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
//
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

//    @Bean
//    public ConsumerFactory<String, Object> consumerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-group");
//        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
//        configProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
//
//        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.tasks.orderservice.kafka.dto, com.example.tasks.paymentservice.kafka.dto");
//        configProps.put(JsonDeserializer.TYPE_MAPPINGS,
//                "OrderCreatedEvent:com.example.tasks.orderservice.kafka.dto.OrderCreatedEvent," +
//                        "PaymentCreatedEvent:com.example.tasks.orderservice.kafka.dto.PaymentCreatedEvent");
//        return new DefaultKafkaConsumerFactory<>(configProps);
//    }
//
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setCommonErrorHandler(new DefaultErrorHandler());
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public NewTopic createOrderTopic() {
        return TopicBuilder
                .name(createOrderTopic)
                .partitions(1)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .build();
    }

    @Bean
    public NewTopic createPaymentTopic() {
        return TopicBuilder
                .name(createPaymentTopic)
                .partitions(1)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .build();
    }
}
