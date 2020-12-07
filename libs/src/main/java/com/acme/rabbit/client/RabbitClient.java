package com.acme.rabbit.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitClient {
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

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
