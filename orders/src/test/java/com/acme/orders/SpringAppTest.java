package com.acme.orders;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAppTest {

  @Autowired
  ApplicationContext context;

  @Test
  public void rabbitBeansShouldBeDeclared() {
    Assert.assertNotNull("The Queue should not be null", context.getBean(Queue.class));
    Assert.assertNotNull("The TopicExchange should not be null", context.getBean(TopicExchange.class));
  }
}