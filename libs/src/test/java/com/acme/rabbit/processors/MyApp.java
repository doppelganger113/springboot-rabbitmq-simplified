package com.acme.rabbit.processors;

import com.acme.rabbit.initializers.SampleSpringApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyApp {
  public static void main(String[] args) {
    SpringApplication.run(SampleSpringApp.class, args);
  }
}
