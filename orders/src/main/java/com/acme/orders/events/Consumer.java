package com.acme.orders.events;

import com.acme.orders.events.models.Burger;
import com.acme.orders.events.models.Order;
import com.acme.rabbit.initializers.ITopicConfig;
import com.acme.rabbit.processors.TopicProcessor;
import com.acme.rabbit.processors.TopicRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class Consumer {

  private final RabbitTemplate rabbitTemplate;
  private final ITopicConfig config;
  private final TopicProcessor topicProcessor;
  private final ObjectMapper objectMapper;

  Consumer(
    RabbitTemplate rabbitTemplate,
    ITopicConfig config,
    ObjectMapper objectMapper
  ) {
    this.rabbitTemplate = rabbitTemplate;
    this.config = config;
    this.topicProcessor = new TopicProcessor(rabbitTemplate, config);
    this.objectMapper = objectMapper;
  }

  @RabbitListener(queues = "orders-queue")
  void handleMessage(
    String body,
    Message message,
    Channel channel,
    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
    @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String receivedRoutingKey
  ) {
    new TopicRouter()
      .on("created",
        msg -> {
          System.out.println(msg);
        },
        error -> {
          System.out.println("An error" + error);
        }
      );
    try {
      log.info("Consumed from queue message: {}", body);
      var value = objectMapper.readValue(body, Order.class);
      log.info("Parsed order: {}", value);
      var burger = (Burger) value;
      log.info("Parsed Burger: {}", burger);
      log.info("Value: {}", (Burger) value);
    } catch (Exception e) {
      log.error("Error parsing the JSON", e);
      e.printStackTrace();
    }
    topicProcessor.process(message, channel, deliveryTag, receivedRoutingKey);
  }
}
