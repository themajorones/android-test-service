package dev.themajorones.autotest.dto.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RabbitMqPublishRequest {

    private String routingKey;
    private String message;
}
