package com.acme.orders.events.processors;

import com.acme.models.Burger;
import com.acme.models.Order;
import com.acme.rabbit.client.RabbitClient;
import com.acme.rabbit.processors.EventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BurgerProcessor implements EventProcessor<Order> {

  private final RabbitClient rabbitClient;

  @Override
  public void process(Order message) throws Exception {
    var burger = (Burger) message;
    log.info("Processing burger: {}", burger);
    log.info("Sending burger to parking lot");
    rabbitClient.send("orders", "orders.updated", burger);
  }

  @Override
  public void onError(Exception e, @Nullable Order value) {
    log.error("Handling the error in my own way!");
    log.error("Got exception: {} for value {}", e.getMessage(), value);
  }
}
