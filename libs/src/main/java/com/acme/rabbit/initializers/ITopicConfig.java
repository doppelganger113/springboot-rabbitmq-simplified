package com.acme.rabbit.initializers;

public interface ITopicConfig {
  String getNamespace();

  int getWaitQueueTtlMillis();

  int getMaxRetries();

  String getExchangeName();

  String getWorkerQueueName();

  String getWaitQueueName();

  String getParkingLotQueueName();

  String getBindingKey();
}
