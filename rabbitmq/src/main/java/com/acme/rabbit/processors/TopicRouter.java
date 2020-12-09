package com.acme.rabbit.processors;

import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registers processing strategies based on the routing key.
 *
 * @param <T>
 */
public class TopicRouter<T> {

  private final Map<String, EventProcessor<T>> routeHandlers = new HashMap<>();

  public TopicRouter<T> on(String routingKey, EventProcessor<T> processor) {
    addRoute(routingKey, processor);
    return this;
  }

  public TopicRouter<T> on(String routingKey, EventProcessing<T> processing) {
    addRoute(routingKey, new EventProcessor<T>() {
      @Override
      public void process(T message) throws Exception {
        processing.process(message);
      }

      @Override
      public void onError(Exception e, @Nullable T value) {
      }
    });
    return this;
  }

  public TopicRouter<T> on(String routingKey, EventProcessing<T> processor, FailureProcessing<T> failureProcessing) {
    routeHandlers.put(routingKey, new EventProcessor<T>() {
      @Override
      public void process(T message) throws Exception {
        processor.process(message);
      }

      @Override
      public void onError(Exception e, @Nullable T value) {
        failureProcessing.onError(e, value);
      }
    });

    return this;
  }

  public Optional<EventProcessor<T>> getEventProcessorByRoute(String route) {
    return Optional.ofNullable(routeHandlers.get(route));
  }

  private void addRoute(String routingKey, EventProcessor<T> eventProcessor) {
    routeHandlers.put(routingKey, eventProcessor);
  }
}
