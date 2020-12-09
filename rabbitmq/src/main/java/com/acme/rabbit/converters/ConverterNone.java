package com.acme.rabbit.converters;

/**
 * Body converter that doesn't do any transformation.
 */
public class ConverterNone implements BodyConverter<String> {
  @Override
  public String convert(String message) {
    return message;
  }
}
