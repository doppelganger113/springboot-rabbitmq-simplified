package com.acme.rabbit.processors;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@RabbitListenerTest
class TopicProcessorTest {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Test
  public void sendBurger() {
  }

}