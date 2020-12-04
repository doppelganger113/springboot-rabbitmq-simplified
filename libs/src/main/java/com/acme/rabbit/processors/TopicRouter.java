package com.acme.rabbit.processors;

public class TopicRouter {

  public void on(String route, EventProcessor processor, EventProcessor errorProcessor) {
    processor.process("Whatevers");
    errorProcessor.process("An error");
  }
}
