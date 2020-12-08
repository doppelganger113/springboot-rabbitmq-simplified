package com.acme.rabbit.initializers;

import lombok.Builder;
import lombok.Data;

/**
 * Used to provide a way of changing the bean names in case the need requires so.
 */
@Data
@Builder
public class TopicBeanNamesConfig {
  public static TopicBeanNamesConfig createDefault() {
    return TopicBeanNamesConfig.builder()
      .exchange("exchange")
      .workerQueue("workerQueue")
      .waitQueue("waitQueue")
      .parkingLotQueue("parkingLotQueue")
      .workerQueueBinding("workerQueueBinding")
      .waitQueueBinding("waitQueueBinding")
      .parkingLotQueueBinding("parkingLotQueueBinding")
      .build();
  }

  private String exchange;
  private String workerQueue;
  private String waitQueue;
  private String parkingLotQueue;
  private String workerQueueBinding;
  private String waitQueueBinding;
  private String parkingLotQueueBinding;
}