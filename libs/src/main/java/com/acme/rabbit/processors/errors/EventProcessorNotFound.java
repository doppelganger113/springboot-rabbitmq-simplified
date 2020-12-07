package com.acme.rabbit.processors.errors;

public class EventProcessorNotFound extends RuntimeException {
  public EventProcessorNotFound(String message) {
    super(message);
  }
}
