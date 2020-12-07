package com.acme.rabbit.processors;

@FunctionalInterface
public interface EventProcessing<T> {
  void process(T message) throws Exception;
}
