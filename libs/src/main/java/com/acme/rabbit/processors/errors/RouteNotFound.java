package com.acme.rabbit.processors.errors;

public class RouteNotFound extends RuntimeException {
  public RouteNotFound(String message) {
    super(message);
  }
}
