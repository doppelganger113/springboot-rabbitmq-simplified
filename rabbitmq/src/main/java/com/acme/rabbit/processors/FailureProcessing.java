package com.acme.rabbit.processors;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface FailureProcessing<T> {
  /**
   * Perform additional error handling.
   */
  void onError(Exception e, @Nullable T value);
}
