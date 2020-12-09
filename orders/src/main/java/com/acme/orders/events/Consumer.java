package com.acme.orders.events;

import com.acme.models.Order;
import com.acme.rabbit.processors.TopicProcessor;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
class Consumer {
  private final TopicProcessor<Order> topicProcessor;

  Consumer(TopicProcessor<Order> topicProcessor) {
    this.topicProcessor = topicProcessor;
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
}
