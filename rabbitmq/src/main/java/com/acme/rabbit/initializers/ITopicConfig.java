package com.acme.rabbit.initializers;

public interface ITopicConfig {
  String getExchange();

  int getWaitQueueTtlMillis();

  int getMaxRetries();

  String getWorkerQueueName();

  String getWaitQueueName();

  String getParkingLotQueueName();

  String getBindingKey();
}
