package com.acme.rabbit.processors.sample;

import com.acme.rabbit.processors.TopicProcessor;
import com.acme.rabbit.processors.sample.models.Animal;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Header;

@RequiredArgsConstructor
@Configuration
public class Listener {
  private final TopicProcessor<Animal> topicProcessor;

  @RabbitListener(queues = "orders-queue")
  public void handleMessage(
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
