package com.acme.rabbit.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * RabbitTemplate wrapper that simplifies conversion of a Class to JSON
 * string and sending it to specified exchange with routing key.
 */
public class RabbitClient {
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  public RabbitClient(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
    this.rabbitTemplate = rabbitTemplate;
    this.objectMapper = objectMapper;
  }

  public void send(String exchange, String routingKey, Object object) throws JsonProcessingException {
    var message = buildMessage(object);
    rabbitTemplate.convertAndSend(exchange, routingKey, message);
  }

  private Message buildMessage(Object object) throws JsonProcessingException {
    var valueAsBytes = objectMapper.writeValueAsBytes(object);

    return MessageBuilder
      .withBody(valueAsBytes)
      .setContentType(MessageProperties.CONTENT_TYPE_JSON)
      .build();
  }
}
