package com.acme.restaurants;

import com.acme.restaurants.events.RestaurantTopicInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringApp {
  public static void main(String[] args) {
    new SpringApplicationBuilder()
      .sources(SpringApp.class)
      .initializers(new RestaurantTopicInitializer())
      .run(args);
  }
}
