package com.acme.restaurants.events.processors;

import com.acme.orders.events.models.Order;
import com.acme.rabbit.processors.EventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BurgerWorker implements EventProcessor<Order> {

  @Override
  public void process(Order message) throws Exception {
    System.out.println("Created burger: " + message.getName());
  }
}
