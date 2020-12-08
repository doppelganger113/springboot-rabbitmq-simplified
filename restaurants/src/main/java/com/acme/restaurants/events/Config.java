package com.acme.restaurants.events;

import com.acme.rabbit.client.RabbitClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class Config {
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  @Bean
  RabbitClient rabbitClient() {
    return new RabbitClient(rabbitTemplate, objectMapper);
  }
}
