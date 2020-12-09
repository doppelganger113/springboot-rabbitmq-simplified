package com.acme.rabbit.converters;

/**
 * Converts the message received to appropriate type before passing it down
 * for further processing.
 * @param <T>
 */
@FunctionalInterface
public interface BodyConverter<T> {
  T convert(String message) throws Exception;
}
