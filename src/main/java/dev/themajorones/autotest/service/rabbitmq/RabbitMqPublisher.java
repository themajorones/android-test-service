package dev.themajorones.autotest.service.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.stereotype.Service;

import dev.themajorones.models.constants.RabbitMqConstant;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitMqPublisher {

    private final RabbitOperations rabbitOperations;

    public void publish(String routingKey, String message) {
        rabbitOperations.convertAndSend(
            RabbitMqConstant.DIRECT_EXCHANGE,
            requireText(routingKey, "RabbitMQ routing key"),
            requireText(message, "RabbitMQ message")
        );
    }

    private String requireText(String value, String description) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(description + " is required");
        }
        return value.trim();
    }
}
