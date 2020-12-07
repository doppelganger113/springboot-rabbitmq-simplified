package com.acme.rabbit.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class CustomInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    var config = new TopicConfig.Builder("orders")
      .withWaitQueueTtlSeconds(5)
      .build();

    var beanNamesConfig = TopicBeanNamesConfig.builder()
      .exchange("custom-exchange")
      .workerQueue("custom-worker")
      .waitQueue("custom-wait")
      .parkingLotQueue("custom-parking-lot-queue")
      .workerQueueBinding("custom-worker-binding")
      .waitQueueBinding("custom-wait-binding")
      .parkingLotQueueBinding("custom-parking-lot-binding")
      .build();

    TopicBeansInitializer.setContextBeans(context, config, beanNamesConfig);
  }
}
