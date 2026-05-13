package dev.themajorones.autotest.dto.rabbitmq;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RabbitMqPublishRequest {

    private String routingKey;
    private String message;
}
