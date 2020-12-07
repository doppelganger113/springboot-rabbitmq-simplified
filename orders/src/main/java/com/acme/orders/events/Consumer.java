package com.acme.orders.events;

import com.acme.orders.events.models.Burger;
import com.acme.orders.events.models.Order;
import com.acme.rabbit.converters.BodyConverter;
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
  private final TopicProcessor<Order> topicProcessor;
  private final ObjectMapper objectMapper;
  private final CustomEventProcessor customEventProcessor;

  public Consumer(
    RabbitTemplate rabbitTemplate,
    ITopicConfig config,
    ObjectMapper objectMapper,
    CustomEventProcessor customEventProcessor) {
    this.objectMapper = objectMapper;
    this.customEventProcessor = customEventProcessor;
    TopicRouter<Order> router = createRouter();

    this.topicProcessor = new TopicProcessor<>(
      rabbitTemplate,
      config,
      router,
      createOrderConverter()
    );
  }

  @RabbitListener(queues = "orders-queue")
  void handleMessage(
    Message message,
    Channel channel,
    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
    @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String receivedRoutingKey
  ) {
    topicProcessor.process(
      message, channel, deliveryTag, receivedRoutingKey
    );
  }

  private BodyConverter<Order> createOrderConverter() {
    return message -> objectMapper.readValue(message, Order.class);
  }

  private TopicRouter<Order> createRouter() {
    return new TopicRouter<Order>()
      .on("orders.created", msg -> {
          log.info("Received created message {}", msg.getName());
          log.info("Casted to Burger {}", ((Burger) msg).getCalories());
        },
        (err, msg) -> {
          log.error("Handling error from created message, err: {} and msg: {}", err, msg);
        }
      )
      .on("orders.updated", msg -> log.info("Just updated without error handling"))
      .on("orders.custom", this.customEventProcessor);
  }
}
