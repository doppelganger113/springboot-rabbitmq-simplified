# Spring boot RabbitMQ simplified

## WORK IN PROGRESS

Solution built of Spring boot [RabbitMQ](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-rabbitmq)
that handles failure through addition of dead-letter wait queue and parking lot queue. It provides easy
setup methods and simple event processing with error handling built-in.

![GitHub Action](https://github.com/doppelganger113/springboot-rabbitmq-simplified/workflows/Java%20CI%20with%20Maven/badge.svg)

## Design
TODO

Table of contents
=================

<!--ts-->
   * [Requirements](#requirements)
   * [Development](#development)
   * [Usage](#usage)
      * [Update application.yml](#1-spring-boot-rabbitmq-configuration)
      * [Create topic and queue initializer](#2-create-topic-and-queue-initializer)
      * [Register initializer in Spring](#3-register-initializer-in-spring)
      * [Configuration](#4-configuration)
      * [Attaching the listener](#5-attaching-the-listener)
      * [Testing with initializer context](#6-testing-with-initializer-context)
<!--te-->

## Requirements
 - Java 11+
 - RabbitMQ 3+
 - Docker and docker-compose (optional)
 
## Development
Start RabbitMQ service via docker:
```bash
docker-compose up -d --build
```

## Usage

### 1. Spring boot RabbitMQ configuration
```yaml
spring:
  rabbitmq:
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    listener:
      simple:
        default-requeue-rejected: false
        acknowledge-mode: manual
        prefetch: 50
```
 
### 2. Create topic and queue initializer
```java
public class OrdersTopicInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  @Override
  public void initialize(GenericApplicationContext context) {
    var config = new TopicConfig.Builder("orders")
      .withWaitQueueTtlSeconds(5)
      .build();
    TopicBeansInitializer.setContextBeans(context, config);
  }
}
```
### 3. Register initializer in Spring
```java
@SpringBootApplication
public class SpringApp {
  public static void main(String[] args) {
    new SpringApplicationBuilder()
      .sources(SpringApp.class)
      .initializers(new OrdersTopicInitializer())
      .run(args);
  }
}
```
### 4. Configuration
   
First configuration is required to avoid circular dependency, you can
place the RabbitClient wherever you like as long as it is not the next
beans configuration class. 
```java
@Configuration
public class Config {
  @Bean
  RabbitClient rabbitClient(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
    return new RabbitClient(rabbitTemplate, objectMapper);
  }
}
```
In the following class we register the service, message converter and most importantly
the route handlers.
```java
@Configuration
@RequiredArgsConstructor
public class TopicConfiguration {

  private final ObjectMapper objectMapper;
  private final RabbitTemplate rabbitTemplate;
  private final ITopicConfig iTopicConfig;
  private final BurgerProcessor burgerProcessor;

  @Bean
  TopicProcessor<Order> topicProcessor() {
    return new TopicProcessor<>(
      rabbitTemplate,
      iTopicConfig,
      topicRouter(),
      bodyConverter()
    );
  }

  @Bean
  TopicRouter<Order> topicRouter() {
    return new TopicRouter<Order>()
      .on("orders.created", burgerProcessor);
  }

  @Bean
  BodyConverter<Order> bodyConverter() {
    return message -> objectMapper.readValue(message, Order.class);
  }
}
```
### 5. Attaching the listener
we can start processing messages based on the routing key.
    ```java
    @Component
    class Consumer {
      private final TopicProcessor<Order> topicProcessor;
    
      Consumer(TopicProcessor<Order> topicProcessor) {
        this.topicProcessor = topicProcessor;
      }
    
      @RabbitListener(queues = "orders-queue")
      void handleMessage(
        Message message,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String receivedRoutingKey
      ) {
        topicProcessor.process(
          message, channel, deliveryTag, receivedRoutingKey
        );
      }
    }
    ```
### 6. Testing with initializer context
If your tests require the RabbitMQ functionality, you must annotate your classes in the
following way:
```java
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SpringApp.class, initializers = MyInitializer.class)
public class SpringAppTest {
    // ...
}
```

## Troubleshooting
TODO