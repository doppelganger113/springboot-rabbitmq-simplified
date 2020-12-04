package com.acme.rabbit.processors;

import org.springframework.amqp.core.Message;

public interface EventProcessor {
  void process(String message);
}