package com.acme.rabbit.initializers;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = SampleSpringApp.class, initializers = CustomInitializer.class)
public class TopicBeansInitializerCustomTest {
  @Autowired
  public ApplicationContext context;

  @Test
  public void validateCustomBeanNames() {
    Assert.assertNotNull(
      "Exchange bean should not be null",
      context.getBean("custom-exchange", TopicExchange.class)
    );

    Assert.assertNotNull(
      "Worker Queue bean should not be null",
      context.getBean("custom-worker", Queue.class)
    );
    Assert.assertNotNull(
      "Wait Queue bean should not be null",
      context.getBean("custom-wait", Queue.class)
    );
    Assert.assertNotNull(
      "Parking Lot Queue bean should not be null",
      context.getBean("custom-parking-lot-queue", Queue.class)
    );

    Assert.assertNotNull(
      "Worker queue binding bean should not be null",
      context.getBean("custom-worker-binding", Binding.class)
    );
    Assert.assertNotNull(
      "Wait queue binding bean should not be null",
      context.getBean("custom-wait-binding", Binding.class)
    );
    Assert.assertNotNull(
      "Worker queue binding bean should not be null",
      context.getBean("custom-parking-lot-binding", Binding.class)
    );
  }
}
