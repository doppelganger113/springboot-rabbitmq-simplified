package com.acme.orders.events;

import com.acme.rabbit.client.RabbitClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

  @Bean
  RabbitClient rabbitClient(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
    return new RabbitClient(rabbitTemplate, objectMapper);
  }
}
