package com.acme.rabbit.initializers;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class SampleInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    var config = new TopicConfig.Builder("orders")
      .withWaitQueueTtlMillis(50)
      .build();
    TopicBeansInitializer.setContextBeans(context, config);
  }
}
