package com.acme.orders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

@Slf4j
public class CustomRabbitMQErrorHandler extends ConditionalRejectingErrorHandler {
  private final RabbitTemplate rabbitTemplate;

  public CustomRabbitMQErrorHandler(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  public void handleError(Throwable t) {
    try {
      super.handleError(t);
    } catch (ImmediateAcknowledgeAmqpException e) {
      t.addSuppressed(e);
      sendToParkingLotQueue(t);
      log.info("Acknowledging that it failed!");
      throw e;
    }

    throw new ImmediateAcknowledgeAmqpException("Queued for retry", t);
  }

  private void sendToParkingLotQueue(Throwable t) {
    var failedMessage = extractMessage(t);
    log.error(
      "Failed message {}, sending message to queue {}",
      failedMessage, "orders-queue.parking-lot"
    );
//    rabbitTemplate.send("orders-queue.parking-lot", failedMessage);
  }

  private Message extractMessage(Throwable t) {
    return ((ListenerExecutionFailedException) t).getFailedMessage();
  }
}
