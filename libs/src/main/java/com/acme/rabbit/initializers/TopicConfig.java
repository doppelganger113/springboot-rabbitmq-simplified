package com.acme.rabbit.initializers;

public class TopicConfig implements ITopicConfig {
  private String namespace;
  private int waitQueueTtlMillis;
  private int maxRetries;
  private String exchangeName;
  private String workerQueueName;
  private String waitQueueName;
  private String parkingLotQueueName;
  private String bindingKey;

  private TopicConfig(String namespace, int waitQueueTtlMillis) {
    this.namespace = namespace;
    this.waitQueueTtlMillis = waitQueueTtlMillis;
  }

  @Override
  public String getNamespace() {
    return this.namespace;
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
  public String getExchangeName() {
    return this.exchangeName;
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
    private final String namespace;

    private int waitQueueTtlMillis = 1_800_000; // 30min
    private int maxRetries = 3;

    private String exchangeName;
    private String workerQueueName;
    private String waitQueueName;
    private String parkingLotQueueName;
    private String bindingKey;

    public Builder(String namespace) {
      this.namespace = namespace;
      setDefaultValues(namespace);
    }

    public Builder withExchange(String exchangeName) {
      this.exchangeName = exchangeName;
      return this;
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
      var config = new TopicConfig(namespace, waitQueueTtlMillis);
      config.namespace = namespace;
      config.exchangeName = exchangeName;
      config.workerQueueName = workerQueueName;
      config.waitQueueName = waitQueueName;
      config.parkingLotQueueName = parkingLotQueueName;
      config.bindingKey = bindingKey;
      config.maxRetries = maxRetries;
      config.waitQueueTtlMillis = waitQueueTtlMillis;
      return config;
    }

    private void setDefaultValues(String namespace) {
      bindingKey = namespace + ".*";
      exchangeName = namespace;
      workerQueueName = namespace + "-queue";
      waitQueueName = workerQueueName + ".wait";
      parkingLotQueueName = this.workerQueueName + ".parking-lot";
    }
  }

  @Override
  public String toString() {
    return "TopicConfig{" +
      "namespace='" + namespace + '\'' +
      ", waitQueueTtlMillis=" + waitQueueTtlMillis +
      ", maxRetries=" + maxRetries +
      ", exchangeName='" + exchangeName + '\'' +
      ", workerQueueName='" + workerQueueName + '\'' +
      ", waitQueueName='" + waitQueueName + '\'' +
      ", parkingLotQueueName='" + parkingLotQueueName + '\'' +
      ", bindingKey='" + bindingKey + '\'' +
      '}';
  }
}
