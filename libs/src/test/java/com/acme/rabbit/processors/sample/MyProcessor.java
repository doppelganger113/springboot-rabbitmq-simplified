package com.acme.rabbit.processors.sample;

import com.acme.rabbit.processors.EventProcessor;
import com.acme.rabbit.processors.sample.models.Animal;
import org.springframework.stereotype.Service;

@Service
public class MyProcessor implements EventProcessor<Animal> {
  @Override
  public void process(Animal message) throws Exception {
  }

  @Override
  public void onError(Exception e, Animal value) {
    System.out.println("Processing errors");
  }
}
