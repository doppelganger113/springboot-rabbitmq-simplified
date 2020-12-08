package com.acme.rabbit.processors;

import com.acme.rabbit.initializers.ITopicConfig;
import com.acme.rabbit.processors.errors.EventProcessorNotFound;
import com.acme.rabbit.processors.sample.Listener;
import com.acme.rabbit.processors.sample.MyProcessor;
import com.acme.rabbit.processors.sample.models.Animal;
import com.acme.rabbit.processors.sample.models.Cat;
import com.acme.rabbit.processors.sample.models.Dog;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.powermock.api.mockito.PowerMockito;
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

  @AfterEach
  @BeforeEach
  public void purgeQueues() {
    amqpAdmin.purgeQueue(iTopicConfig.getWorkerQueueName());
    amqpAdmin.purgeQueue(iTopicConfig.getWaitQueueName());
    amqpAdmin.purgeQueue(iTopicConfig.getParkingLotQueueName());
  }

  private void assertNoRejections() throws Exception {
    verify(myProcessor, times(0)).onError(any(), any());

    PowerMockito.verifyPrivate(topicProcessor, times(0))
      .invoke("rejectMessage", any(), any(), anyLong(), any());

    PowerMockito.verifyPrivate(topicProcessor, times(0))
      .invoke("shovelMessageToParkingLotQueue", any(), any(), anyLong());

    PowerMockito.verifyPrivate(topicProcessor, times(1))
      .invoke("acknowledgeProcessedMessage", any(), any(), anyLong());
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

    assertNoRejections();
  }

  @Test
  public void sendDog() throws Exception {
    this.rabbitTemplate.convertSendAndReceive(
      "orders",
      "orders.custom",
      "{\"name\":\"Billy\", \"type\":\"Dog\", \"breed\":\"Labrador\"}"
    );
    var expectedAnimal = new Dog("Billy", "Labrador");
    verify(myProcessor).process(expectedAnimal);

    assertNoRejections();
  }

  @Test
  public void noRouteProcessorError() throws Exception {
    this.rabbitTemplate.convertSendAndReceive(
      "orders",
      "orders.done",
      "{\"name\":\"Billy\", \"type\":\"Dog\", \"breed\":\"Labrador\"}"
    );

    PowerMockito.verifyPrivate(topicProcessor, times(3))
      .invoke("rejectMessage", any(), any(), anyLong(), any(EventProcessorNotFound.class));

    PowerMockito.verifyPrivate(topicProcessor, times(1))
      .invoke("shovelMessageToParkingLotQueue", any(), any(), anyLong());

    PowerMockito.verifyPrivate(topicProcessor, times(1))
      .invoke("acknowledgeProcessedMessage", any(), any(), anyLong());
  }

  @Test
  public void invalidJsonError() throws Exception {
    this.rabbitTemplate.convertSendAndReceive(
      "orders",
      "orders.created",
      "{\"name\":\"Billy\", \"type\":\"Dog\", \"breed\":\"Labrador\""
    );

    PowerMockito.verifyPrivate(topicProcessor, times(3))
      .invoke("rejectMessage", any(), any(), anyLong(), any(JsonEOFException.class));

    PowerMockito.verifyPrivate(topicProcessor, times(1))
      .invoke("shovelMessageToParkingLotQueue", any(), any(), anyLong());

    PowerMockito.verifyPrivate(topicProcessor, times(1))
      .invoke("acknowledgeProcessedMessage", any(), any(), anyLong());
  }

  @Test
  public void processorShouldHandleOnError() throws Exception {
    this.rabbitTemplate.convertSendAndReceive(
      "orders",
      "orders.custom",
      "{\"type\":\"Dog\", \"breed\":\"Labrador\"}"
    );

    var expectedAnimal = new Dog(null, "Labrador");

    spy(myProcessor).onError(any(NullPointerException.class), eq(expectedAnimal));

    PowerMockito.verifyPrivate(topicProcessor, times(3))
      .invoke("rejectMessage", any(), any(), anyLong(), any(NullPointerException.class));

    PowerMockito.verifyPrivate(topicProcessor, times(1))
      .invoke("shovelMessageToParkingLotQueue", any(), any(), anyLong());

    PowerMockito.verifyPrivate(topicProcessor, times(1))
      .invoke("acknowledgeProcessedMessage", any(), any(), anyLong());
  }
}