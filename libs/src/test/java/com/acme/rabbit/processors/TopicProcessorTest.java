package com.acme.rabbit.processors;

import com.acme.rabbit.initializers.ITopicConfig;
import com.acme.rabbit.processors.sample.Listener;
import com.acme.rabbit.processors.sample.MyProcessor;
import com.acme.rabbit.processors.sample.models.Animal;
import com.acme.rabbit.processors.sample.models.Cat;
import com.acme.rabbit.processors.sample.models.Dog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TopicProcessorTest {
  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Autowired
  private RabbitListenerTestHarness harness;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Listener listener;

  @SpyBean
  private MyProcessor myProcessor;

  @SpyBean
  private TopicProcessor<Animal> topicProcessor;

  @Autowired
  private ITopicConfig iTopicConfig;

//  public TopicProcessorTest(
//    RabbitTemplate rabbitTemplate,
//    ITopicConfig iTopicConfig,
//    MyProcessor myProcessor,
//    ObjectMapper objectMapper,
//    AmqpAdmin amqpAdmin,
//    RabbitListenerTestHarness harness
//    ) {
//    this.rabbitTemplate = rabbitTemplate;
//    this.iTopicConfig = iTopicConfig;
//    this.objectMapper = objectMapper;
//    this.myProcessor = myProcessor;
//    this.amqpAdmin = amqpAdmin;
//    this.harness = harness;
//    var topicProcessor = new TopicProcessor<>(
//      rabbitTemplate,
//      iTopicConfig,
//      objectMapper,
//      myProcessor
//    );
//    this.listener = new Listener();
//  }

  @AfterEach
  @BeforeEach
  public void purgeQueues() {
    amqpAdmin.purgeQueue(iTopicConfig.getWorkerQueueName());
    amqpAdmin.purgeQueue(iTopicConfig.getWaitQueueName());
    amqpAdmin.purgeQueue(iTopicConfig.getParkingLotQueueName());
  }

  @Test
  public void sendCat() throws Exception {
    this.rabbitTemplate.convertSendAndReceive(
      "orders",
      "orders.created",
      "{\"name\":\"Mr.Skitters\", \"type\":\"Cat\", \"age\":3}"
    );
    var expectedAnimal = new Cat("Mr.Skitters", 3);
    verify(myProcessor).process(expectedAnimal);
  }

  @Test
  public void sendDog() throws Exception {
    this.rabbitTemplate.convertSendAndReceive(
      "orders",
      "orders.created",
      "{\"name\":\"Billy\", \"type\":\"Dog\", \"breed\":\"Labrador\"}"
    );
    var expectedAnimal = new Dog("Billy", "Labrador");
    verify(myProcessor).process(expectedAnimal);
  }

//  @Test
//  public void noRouteProcessorError() throws Exception {
//    this.rabbitTemplate.convertSendAndReceive(
//      "orders",
//      "orders.done",
//      "{\"name\":\"Billy\", \"type\":\"Dog\", \"breed\":\"Labrador\"}"
//    );
//
//    var expectedAnimal = new Dog("Billy", "Labrador");
//    PowerMockito.verifyPrivate(spiedTopicProcessor)
//      .invoke("rejectMessage", any(), any(), any(), "");
//
////      . (new RouteNotFound("orders.done"), expectedAnimal);
//  }
}