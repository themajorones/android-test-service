package dev.themajorones.autotest.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.themajorones.autotest.dto.rabbitmq.RabbitMqPublishRequest;
import dev.themajorones.autotest.service.rabbitmq.RabbitMqPublisher;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rabbitmq/messages")
@RequiredArgsConstructor
public class RabbitMqResource {

    private final RabbitMqPublisher publisher;

    @PostMapping
    public ResponseEntity<Void> publish(@RequestBody RabbitMqPublishRequest request) {
        publisher.publish(request.getRoutingKey(), request.getMessage());
        return ResponseEntity.accepted().build();
    }
}
