package com.acme.rabbit.converters;

@FunctionalInterface
public interface BodyConverter<T> {
  T convert(String message) throws Exception;
}
