package dev.themajorones.autotest.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.themajorones.models.constants.RabbitMqConstant;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(RabbitMqConstant.DIRECT_EXCHANGE);
    }
}
