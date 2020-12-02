package com.acme.orders.events;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderEventsConfig {
  // Exchanges
  public static final String EXCHANGE = "orders";
  // Queues
  public static final String QUEUE = "orders-queue";
  public static final String QUEUE_WAIT = QUEUE + ".wait";
  public static final String QUEUE_PARKING_LOT = QUEUE + ".parking-lot";
  // Bindings
  public static final String BINDING_KEY_DEFAULT = "orders.*";
  // Routes
  public static final String ROUTE_CREATE = "orders.create";
  public static final String ROUTE_UPDATE = "orders.update";
  // Options
  public static final int TTL_WAIT_QUEUE_DURATION_IN_MS = 5_000; // 1_800_000 or 1_000*60*30 = 30min
  public static final int MAX_RETRIES = 3;

  @Bean
  TopicExchange customExchange() {
    return new TopicExchange(EXCHANGE);
  }

  @Bean
  Queue workingQueue() {
    return QueueBuilder.durable(QUEUE)
      .deadLetterExchange(EXCHANGE)
      .deadLetterRoutingKey(QUEUE_WAIT)
      .build();
  }

  @Bean
  Queue waitQueue() {
    return QueueBuilder.durable(QUEUE_WAIT)
      .deadLetterExchange(EXCHANGE)
      .deadLetterRoutingKey(BINDING_KEY_DEFAULT)
      .ttl(TTL_WAIT_QUEUE_DURATION_IN_MS)
      .build();
  }

  @Bean
  Queue parkingLotQueue() {
    return QueueBuilder.durable(QUEUE_PARKING_LOT)
      .build();
  }

  @Bean
  Binding workingBinding() {
    return BindingBuilder
      .bind(workingQueue())
      .to(customExchange())
      .with(BINDING_KEY_DEFAULT);
  }

  @Bean
  Binding waitBinding() {
    return BindingBuilder
      .bind(waitQueue())
      .to(customExchange())
      .with(QUEUE_WAIT);
  }

  @Bean
  Binding parkingLotBinding() {
    return BindingBuilder
      .bind(parkingLotQueue())
      .to(customExchange())
      .with(QUEUE_PARKING_LOT);
  }
}
