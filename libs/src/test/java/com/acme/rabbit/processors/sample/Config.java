package com.acme.rabbit.processors.sample;

import com.acme.rabbit.converters.BodyConverter;
import com.acme.rabbit.initializers.ITopicConfig;
import com.acme.rabbit.processors.TopicProcessor;
import com.acme.rabbit.processors.TopicRouter;
import com.acme.rabbit.processors.sample.models.Animal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RabbitListenerTest
public class Config {

  @Bean
  TopicProcessor<Animal> topicProcessor(
    ITopicConfig iTopicConfig,
    RabbitTemplate rabbitTemplate,
    MyProcessor myProcessor,
    ObjectMapper objectMapper
  ) {
    return new TopicProcessor<>(
      rabbitTemplate,
      iTopicConfig,
      topicRouter(myProcessor),
      bodyConverter(objectMapper)
    );
  }

  @Bean
  TopicRouter<Animal> topicRouter(MyProcessor myProcessor) {
    return new TopicRouter<Animal>()
      .on("orders.created", myProcessor::process,
        myProcessor::onError
      )
      .on("orders.custom", myProcessor)
      ;
  }

  @Bean
  BodyConverter<Animal> bodyConverter(ObjectMapper objectMapper) {
    return message -> objectMapper.readValue(message, Animal.class);
  }
}
