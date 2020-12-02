package com.acme.orders.events;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
class Consumer {
  private static final boolean REJECT_MULTIPLE_DELIVERIES = false;
  private static final boolean REQUEUE_REJECTED_MESSAGES = false;

  private final RabbitTemplate rabbitTemplate;
  private static final boolean ACKNOWLEDGE_MULTIPLE_DELIVERIES = false;

  @RabbitListener(queues = OrderEventsConfig.QUEUE)
  void handleMessage(
    Message message,
    Channel channel,
    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
    @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String receivedRoutingKey
  ) {
    try {
      log.info(
        "receivedRoutingKey {} - RabbitListener received message '{}' on channel {}",
        receivedRoutingKey, message, channel
      );
      checkMessagePersistence(message);

      var routingKey = extractRoutingKeyFromMessage(message, receivedRoutingKey);
      log.info("Routing key: {}", routingKey);

      // Must be called after retrieving the routing key to not erase data
      if (isMessageFromParkingLotQueue(receivedRoutingKey)) {
        message.getMessageProperties().getXDeathHeader().clear();
        log.info("Cleared headers in msg: {}", message);
      }

      if (hasExceededMaxRetry(message)) {
        handleExceededMaxRetry(message, channel, deliveryTag);
        return;
      }

      process(message);

      acknowledgeProcessedMessage(message, channel, deliveryTag);
    } catch (Exception e) {
      log.error("Processing failed with error: {}", e.getMessage());
      e.printStackTrace();
      rejectMessage(message, channel, deliveryTag, e);
    }
  }

  private void rejectMessage(Message message, Channel channel, long deliveryTag, Exception e) {
    try {
      channel.basicNack(deliveryTag, REJECT_MULTIPLE_DELIVERIES, REQUEUE_REJECTED_MESSAGES);
      log.info("NACK-end message {} because of exception {}", message, e.getMessage());
    } catch (IOException ioException) {
      log.error("Could not NACK message {} due to IOException {}", message, ioException.getMessage());
      ioException.printStackTrace();
    }
  }

  private boolean isMessageFromWaitQueue(String receivedRoutingKey) {
    return receivedRoutingKey.equals(OrderEventsConfig.BINDING_KEY_DEFAULT);
  }

  private boolean isMessageFromParkingLotQueue(String receivedRoutingKey) {
    return receivedRoutingKey.equals(OrderEventsConfig.QUEUE);
  }

  private void handleExceededMaxRetry(Message message, Channel channel, long deliveryTag) {
    log.info("MaxRetries exceeded.");
    shovelMessageToQueue(channel, message, deliveryTag);
    log.info(
      "Generating e-mail to inform that we have exceeded MaxRetry for message '{}'.",
      message
    );
  }

  void process(Message message) {
    var transferObject = new String(message.getBody(), StandardCharsets.UTF_8);
    log.info("Processing transfer object: {}", transferObject);
    if (transferObject.equals("error")) {
      throw new RuntimeException("Testing an error");
    }
    log.info("Finished processing the message");
  }

  void acknowledgeProcessedMessage(
    Message message,
    Channel channel,
    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
  ) {
    try {
      log.info(
        "{} - RabbitListener acknowledges delivery tag: {}",
        channel.getChannelNumber(),
        deliveryTag
      );
      channel.basicAck(deliveryTag, ACKNOWLEDGE_MULTIPLE_DELIVERIES);
    } catch (IOException e) {
      log.error("Could NOT send ACK to RabbitMQ. Concerned message is: '{}'", message);
      e.printStackTrace();
    }
  }

  void checkMessagePersistence(Message message) {
    var hasMessagePersistence = MessageDeliveryMode.PERSISTENT.equals(
      message.getMessageProperties().getReceivedDeliveryMode()
    );

    if (!hasMessagePersistence) {
      log.error(
        "MessageDeliveryMode is not set to PERSISTENT. Inform the publisher to fix this."
      );
    }
  }

  boolean hasExceededMaxRetry(Message message) {
    List<Map<String, ?>> xDeathHeader = message.getMessageProperties().getXDeathHeader();
    if (Objects.isNull(xDeathHeader) || xDeathHeader.size() == 0) {
      return false;
    }

    for (final Map<String, ?> headerMap : xDeathHeader) {
      var isFromWaitQueue = isDeathHeaderFrom(
        headerMap, OrderEventsConfig.QUEUE_WAIT
      );
      if (isFromWaitQueue) {
        return hasExceededCount(headerMap);
      }
    }

    return false;
  }

  private boolean hasExceededCount(Map<String, ?> headerMap) {
    var count = (Long) headerMap.get("count");
    return count >= OrderEventsConfig.MAX_RETRIES;
  }

  private boolean isDeathHeaderFrom(Map<String, ?> headerMap, String queue) {
    var exchangeFromHeaderMap = (String) headerMap.get("exchange");
    var queueFromHeaderMap = (String) headerMap.get("queue");

    var areExchangesEqual = OrderEventsConfig.EXCHANGE.equals(exchangeFromHeaderMap);
    var areQueuesEqual = queue.equals(queueFromHeaderMap);

    return areExchangesEqual && areQueuesEqual;
  }

  void shovelMessageToQueue(Channel channel, Message failedMessage, long deliveryTag) {
    log.info("Putting message '{}' into queue '{}'", failedMessage, OrderEventsConfig.QUEUE_PARKING_LOT);

    failedMessage.getMessageProperties()
      .setDeliveryMode(MessageDeliveryMode.PERSISTENT);

    this.rabbitTemplate.send(OrderEventsConfig.QUEUE_PARKING_LOT, failedMessage);
    acknowledgeProcessedMessage(failedMessage, channel, deliveryTag);
  }

  Optional<String> extractRoutingKeyFromMessage(Message message, String receivedRoutingKey) {
    if (isMessageFromWaitQueue(receivedRoutingKey)) {
      log.info("FROM WAIT QUEUE");
      return extractRoutingKeyFromMessageByQueue(message);
    } else if (isMessageFromParkingLotQueue(receivedRoutingKey)) {
      log.info("FROM PARKING LOT QUEUE");
      return extractRoutingKeyFromMessageByQueue(message);
    }

    log.info("FROM MAIN QUEUE");
    return Optional.of(receivedRoutingKey);
  }

  Optional<String> extractRoutingKeyFromMessageByQueue(Message message) {
    return Optional.ofNullable(
      message.getMessageProperties().getXDeathHeader()
    )
      .filter(xDeathHeader -> xDeathHeader.size() > 0)
      .flatMap(xDeathHeader ->
        xDeathHeader.stream()
          .filter(headersMap ->
            isDeathHeaderFrom(headersMap, OrderEventsConfig.QUEUE)
          )
          .map(this::extractRoutingKeyFromXDeathHeader)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findFirst()
      );
  }

  private Optional<String> extractRoutingKeyFromXDeathHeader(Map<String, ?> headersMap) {
    @SuppressWarnings("unchecked")
    var routingKeys = (ArrayList<String>) headersMap.get("routing-keys");
    if (routingKeys == null || routingKeys.size() == 0) {
      return Optional.empty();
    }

    return Optional.of(
      routingKeys.get(0)
    );
  }
}
