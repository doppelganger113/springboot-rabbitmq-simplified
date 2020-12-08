package com.acme.rabbit.initializers;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class TopicBeansInitializerTest {

  @Autowired
  private ApplicationContext context;

  @Test
  public void validateBeans() {
    Assert.assertNotNull(
      "Exchange bean should not be null",
      context.getBean("exchange", TopicExchange.class)
    );

    Assert.assertNotNull(
      "Worker Queue bean should not be null",
      context.getBean("workerQueue", Queue.class)
    );
    Assert.assertNotNull(
      "Wait Queue bean should not be null",
      context.getBean("waitQueue", Queue.class)
    );
    Assert.assertNotNull(
      "Parking Lot Queue bean should not be null",
      context.getBean("parkingLotQueue", Queue.class)
    );

    Assert.assertNotNull(
      "Worker queue binding bean should not be null",
      context.getBean("workerQueueBinding", Binding.class)
    );
    Assert.assertNotNull(
      "Wait queue binding bean should not be null",
      context.getBean("waitQueueBinding", Binding.class)
    );
    Assert.assertNotNull(
      "Worker queue binding bean should not be null",
      context.getBean("parkingLotQueueBinding", Binding.class)
    );
  }
}