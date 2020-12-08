package com.acme.orders.events;

import com.acme.orders.events.models.Order;
import com.acme.orders.events.processors.BurgerProcessor;
import com.acme.rabbit.converters.BodyConverter;
import com.acme.rabbit.initializers.ITopicConfig;
import com.acme.rabbit.processors.TopicProcessor;
import com.acme.rabbit.processors.TopicRouter;
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
  private final BurgerProcessor burgerProcessor;

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
      .on("orders.created", burgerProcessor);
  }

  @Bean
  BodyConverter<Order> bodyConverter() {
    return message -> objectMapper.readValue(message, Order.class);
  }
}
