package com.acme.rabbit.initializers;

import org.junit.Assert;
import org.junit.Test;

public class TopicConfigTest {

  @Test
  public void shouldBuildDefault() {
    ITopicConfig config = new TopicConfig.Builder("orders")
      .build();

    Assert.assertNotNull(config);
    Assert.assertEquals("namespace should be as expected", config.getExchange(), "orders");
    Assert.assertEquals(config.getWorkerQueueName(), "orders-queue");
    Assert.assertEquals(config.getWaitQueueName(), "orders-queue.wait");
    Assert.assertEquals(config.getParkingLotQueueName(), "orders-queue.parking-lot");
    Assert.assertEquals(config.getBindingKey(), "orders.*");
    Assert.assertEquals("exchangeName should be as expected", config.getExchange(), "orders");
    Assert.assertEquals(config.getMaxRetries(), 3);
    Assert.assertEquals(config.getWaitQueueTtlMillis(), 1_800_000);
  }

  @Test
  public void shouldBuildWithDifferentTimeUnits() {
    var config = new TopicConfig.Builder("orders")
      .withWaitQueueTtlMillis(1_000)
      .build();
    Assert.assertEquals(config.getWaitQueueTtlMillis(), 1_000);

    config = new TopicConfig.Builder("orders")
      .withWaitQueueTtlSeconds(5)
      .build();
    Assert.assertEquals(config.getWaitQueueTtlMillis(), 5_000);

    config = new TopicConfig.Builder("orders")
      .withWaitQueueTtlMinutes(10)
      .build();
    Assert.assertEquals(config.getWaitQueueTtlMillis(), 600_000);

    config = new TopicConfig.Builder("orders")
      .withWaitQueueTtlHours(2)
      .build();
    Assert.assertEquals(config.getWaitQueueTtlMillis(), 7_200_000);
  }

  @Test
  public void shouldBuildCustom() {
    ITopicConfig config = new TopicConfig.Builder("orders")
      .withWorkerQueueName("my-worker-queue")
      .withWaitQueueName("my-wait-queue")
      .withParkingLotQueueName("my-parking-lot")
      .withWaitQueueTtlSeconds(5)
      .onBindingKey("*.default")
      .onMaxRetries(5)
      .build();

    Assert.assertEquals(config.getExchange(), "my-exchange");
    Assert.assertEquals(config.getWorkerQueueName(), "my-worker-queue");
    Assert.assertEquals(config.getWaitQueueName(), "my-wait-queue");
    Assert.assertEquals(config.getParkingLotQueueName(), "my-parking-lot");
    Assert.assertEquals(config.getWaitQueueTtlMillis(), 5_000);
    Assert.assertEquals(config.getBindingKey(), "*.default");
    Assert.assertEquals(config.getMaxRetries(), 5);
  }
}