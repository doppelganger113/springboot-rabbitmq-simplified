package com.acme.rabbit.converters;

public class ConverterNone implements BodyConverter<String> {
  @Override
  public String convert(String message) throws Exception {
    return message;
  }
}
