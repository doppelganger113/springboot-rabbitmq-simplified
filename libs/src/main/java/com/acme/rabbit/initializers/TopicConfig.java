package com.acme.rabbit.initializers;

public class TopicConfig implements ITopicConfig {
  private int waitQueueTtlMillis;
  private int maxRetries;
  private String exchange;
  private String workerQueueName;
  private String waitQueueName;
  private String parkingLotQueueName;
  private String bindingKey;

  private TopicConfig(String exchange, int waitQueueTtlMillis) {
    this.exchange = exchange;
    this.waitQueueTtlMillis = waitQueueTtlMillis;
  }

  @Override
  public String getExchange() {
    return this.exchange;
  }

  @Override
  public int getWaitQueueTtlMillis() {
    return this.waitQueueTtlMillis;
  }

  @Override
  public int getMaxRetries() {
    return this.maxRetries;
  }

  @Override
  public String getWorkerQueueName() {
    return this.workerQueueName;
  }

  @Override
  public String getWaitQueueName() {
    return this.waitQueueName;
  }

  @Override
  public String getParkingLotQueueName() {
    return this.parkingLotQueueName;
  }

  @Override
  public String getBindingKey() {
    return this.bindingKey;
  }

  public static class Builder {

    private int waitQueueTtlMillis = 1_800_000; // 30min
    private int maxRetries = 3;

    private final String exchange;
    private String workerQueueName;
    private String waitQueueName;
    private String parkingLotQueueName;
    private String bindingKey;

    public Builder(String exchange) {
      this.exchange = exchange;
      setDefaultValues(exchange);
    }

    public Builder withWorkerQueueName(String workerQueueName) {
      this.workerQueueName = workerQueueName;
      return this;
    }

    public Builder withWaitQueueName(String waitQueueName) {
      this.waitQueueName = waitQueueName;
      return this;
    }

    public Builder withParkingLotQueueName(String parkingLotQueueName) {
      this.parkingLotQueueName = parkingLotQueueName;
      return this;
    }

    public Builder onBindingKey(String bindingKey) {
      this.bindingKey = bindingKey;
      return this;
    }

    public Builder onMaxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    public Builder withWaitQueueTtlMillis(int waitQueueTtlMillis) {
      this.waitQueueTtlMillis = waitQueueTtlMillis;
      return this;
    }

    public Builder withWaitQueueTtlSeconds(int waitQueueTtlSeconds) {
      this.waitQueueTtlMillis = 1_000 * waitQueueTtlSeconds;
      return this;
    }

    public Builder withWaitQueueTtlMinutes(int waitQueueTtlMinutes) {
      this.waitQueueTtlMillis = 1_000 * 60 * waitQueueTtlMinutes;
      return this;
    }

    public Builder withWaitQueueTtlHours(int waitQueueTtlHours) {
      this.waitQueueTtlMillis = 1_000 * 60 * 60 * waitQueueTtlHours;
      return this;
    }

    public TopicConfig build() {
      var config = new TopicConfig(exchange, waitQueueTtlMillis);
      config.exchange = exchange;
      config.workerQueueName = workerQueueName;
      config.waitQueueName = waitQueueName;
      config.parkingLotQueueName = parkingLotQueueName;
      config.bindingKey = bindingKey;
      config.maxRetries = maxRetries;
      config.waitQueueTtlMillis = waitQueueTtlMillis;
      return config;
    }

    private void setDefaultValues(String exchange) {
      bindingKey = exchange + ".*";
      workerQueueName = exchange + "-queue";
      waitQueueName = workerQueueName + ".wait";
      parkingLotQueueName = this.workerQueueName + ".parking-lot";
    }
  }

  @Override
  public String toString() {
    return "TopicConfig{" +
      "exchangeName='" + exchange + '\'' +
      ", waitQueueTtlMillis=" + waitQueueTtlMillis +
      ", maxRetries=" + maxRetries +
      ", exchangeName='" + exchange + '\'' +
      ", workerQueueName='" + workerQueueName + '\'' +
      ", waitQueueName='" + waitQueueName + '\'' +
      ", parkingLotQueueName='" + parkingLotQueueName + '\'' +
      ", bindingKey='" + bindingKey + '\'' +
      '}';
  }
}
