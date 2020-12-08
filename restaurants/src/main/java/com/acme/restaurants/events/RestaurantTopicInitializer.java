package com.acme.restaurants.events;

import com.acme.rabbit.initializers.TopicBeansInitializer;
import com.acme.rabbit.initializers.TopicConfig;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class RestaurantTopicInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    var config = new TopicConfig.Builder("restaurant")
      .withWaitQueueTtlSeconds(5)
      .build();
    TopicBeansInitializer.setContextBeans(context, config);
  }
}