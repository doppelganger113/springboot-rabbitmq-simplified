package com.acme.orders;

import com.acme.orders.events.OrdersTopicInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringApp {
  public static void main(String[] args) {
    new SpringApplicationBuilder()
      .sources(SpringApp.class)
      .initializers(new OrdersTopicInitializer())
      .run(args);
  }
}
