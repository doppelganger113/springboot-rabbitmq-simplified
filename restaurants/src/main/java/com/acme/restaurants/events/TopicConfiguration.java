package com.acme.restaurants.events;

import com.acme.orders.events.models.Order;

import com.acme.rabbit.converters.BodyConverter;
import com.acme.rabbit.initializers.ITopicConfig;
import com.acme.rabbit.processors.TopicProcessor;
import com.acme.rabbit.processors.TopicRouter;
import com.acme.restaurants.events.processors.BurgerWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TopicConfiguration {

  private final ObjectMapper objectMapper;
  private final RabbitTemplate rabbitTemplate;
  private final ITopicConfig iTopicConfig;
  private final BurgerWorker burgerWorker;

  @Bean
  TopicProcessor<Order> topicProcessor() {
    return new TopicProcessor<>(
      rabbitTemplate,
      iTopicConfig,
      topicRouter(),
      bodyConverter()
    );
  }

  @Bean
  TopicRouter<Order> topicRouter() {
    return new TopicRouter<Order>()
      .on("restaurant.created", burgerWorker);
  }

  @Bean
  BodyConverter<Order> bodyConverter() {
    return message -> objectMapper.readValue(message, Order.class);
  }
}