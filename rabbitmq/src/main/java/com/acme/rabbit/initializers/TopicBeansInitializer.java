package com.acme.rabbit.initializers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Used with an initializer class that implements <code>ApplicationContextInitializer<GenericApplicationContext></code>
 * and is bootstrapped with Spring by specifying that class through file "resources/META-INF/spring.factories":
 * <code>org.springframework.context.ApplicationContextInitializer=com.acme.orders.events.RabbitTopicBeanInitializer</code>
 * <p>
 * Within the method implementation you would set the context for example:
 * <code>
 * public class RabbitTopicBeanInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
 *
 * @Override public void initialize(GenericApplicationContext context) {
 * TopicBeansInitializer.setContextBeans(context, "orders");
 * }
 * }
 * </code>
 * There are additional method signatures where you can set custom values for queues and for created beans.
 * @see TopicBeansInitializer#setContextBeans(GenericApplicationContext, ITopicConfig)
 * @see TopicBeansInitializer#setContextBeans(GenericApplicationContext, ITopicConfig, TopicBeanNamesConfig)
 */
@Slf4j
public class TopicBeansInitializer {
  public static void setContextBeans(GenericApplicationContext context, String namespace) {
    ITopicConfig config = new TopicConfig.Builder(namespace)
      .build();
    var beanNames = TopicBeanNamesConfig.createDefault();

    log.debug(
      "[{}] initialized with config {} and beans {}",
      TopicBeansInitializer.class, config, beanNames);
    setContextBeans(context, config, beanNames);
  }

  public static void setContextBeans(GenericApplicationContext context, ITopicConfig config) {
    var beanNames = TopicBeanNamesConfig.createDefault();
    setContextBeans(context, config, beanNames);
  }

  public static void setContextBeans(
    GenericApplicationContext context, ITopicConfig config, TopicBeanNamesConfig beanNames
  ) {
    // Configuration
    context.registerBean(ITopicConfig.class, () -> config);

    // Exchange
    var exchange = new TopicExchange(config.getExchange());
    context.registerBean(beanNames.getExchange(), TopicExchange.class, () -> exchange);

    // Queues
    var workingQueue = QueueBuilder.durable(config.getWorkerQueueName())
      .deadLetterExchange(config.getExchange())
      .deadLetterRoutingKey(config.getWaitQueueName())
      .build();

    var waitQueue = QueueBuilder.durable(config.getWaitQueueName())
      .deadLetterExchange(config.getExchange())
      .deadLetterRoutingKey(config.getBindingKey())
      .ttl(config.getWaitQueueTtlMillis())
      .build();

    var parkingLotQueue = QueueBuilder.durable(config.getParkingLotQueueName())
      .build();

    context.registerBean(beanNames.getWorkerQueue(), Queue.class, () -> workingQueue);
    context.registerBean(beanNames.getWaitQueue(), Queue.class, () -> waitQueue);
    context.registerBean(beanNames.getParkingLotQueue(), Queue.class, () -> parkingLotQueue);

    // Bindings
    var workingQueueBinding = BindingBuilder
      .bind(workingQueue)
      .to(exchange)
      .with(config.getBindingKey());

    var waitQueueBinding = BindingBuilder
      .bind(waitQueue)
      .to(exchange)
      .with(config.getWaitQueueName());

    var parkingLotBinding = BindingBuilder
      .bind(parkingLotQueue)
      .to(exchange)
      .with(config.getParkingLotQueueName());

    context.registerBean(beanNames.getWorkerQueueBinding(), Binding.class, () -> workingQueueBinding);
    context.registerBean(beanNames.getWaitQueueBinding(), Binding.class, () -> waitQueueBinding);
    context.registerBean(beanNames.getParkingLotQueueBinding(), Binding.class, () -> parkingLotBinding);
  }
}
