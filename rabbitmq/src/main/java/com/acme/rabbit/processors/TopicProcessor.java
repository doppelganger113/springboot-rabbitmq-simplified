package com.acme.rabbit.processors;

import com.acme.rabbit.converters.BodyConverter;
import com.acme.rabbit.initializers.ITopicConfig;
import com.acme.rabbit.processors.errors.EventProcessorNotFound;
import com.acme.rabbit.processors.errors.RouteNotFound;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Handles message processing by doing acknowledgment, rejection, executing appropriate
 * message processors based on routing key and handling exceptions. Performs retry logic
 * and parking-lot queue shovelling after retry failed more than specified amount of time.
 *
 * @param <T>
 */
@Slf4j
public class TopicProcessor<T> {
  private static final boolean REJECT_MULTIPLE_DELIVERIES = false;
  private static final boolean REQUEUE_REJECTED_MESSAGES = false;
  private static final boolean ACKNOWLEDGE_MULTIPLE_DELIVERIES = false;

  private final RabbitTemplate rabbitTemplate;
  private final ITopicConfig topicConfig;
  private final TopicRouter<T> topicRouter;
  private final BodyConverter<T> converter;

  public TopicProcessor(
    RabbitTemplate rabbitTemplate,
    ITopicConfig topicConfig,
    TopicRouter<T> topicRouter,
    BodyConverter<T> converter
  ) {
    this.rabbitTemplate = rabbitTemplate;
    this.topicConfig = topicConfig;
    this.topicRouter = topicRouter;
    this.converter = converter;
  }

  public void process(
    Message msg,
    Channel channel,
    long deliveryTag,
    String receivedRoutingKey) {
    var body = new String(msg.getBody(), StandardCharsets.UTF_8);

    Optional<EventProcessor<T>> optionalProcessor = Optional.empty();
    T converted = null;

    try {
      log.debug(
        "receivedRoutingKey {} - RabbitListener received message '{}' on channel {}",
        receivedRoutingKey, msg, channel
      );
      checkMessagePersistence(msg);

      var optionalRoutingKey = extractRoutingKeyFromMessage(msg, receivedRoutingKey);

      // Must be called after retrieving the routing key to not erase data
      if (isMessageFromParkingLotQueue(receivedRoutingKey)) {
        if (msg.getMessageProperties().getXDeathHeader() != null) {
          msg.getMessageProperties().getXDeathHeader().clear();
        }
      }

      if (hasExceededMaxRetry(msg)) {
        handleExceededMaxRetry(msg, channel, deliveryTag);
        return;
      }

      // We want to throw only after the message checks are done, to prevent infinite requeue
      var routingKey = optionalRoutingKey
        .orElseThrow(() -> new RouteNotFound(receivedRoutingKey));

      optionalProcessor = topicRouter.getEventProcessorByRoute(routingKey);
      var processor = optionalProcessor
        .orElseThrow(() ->
          new EventProcessorNotFound("Could not find processor for route: " + routingKey));

      converted = converter.convert(body);
      processor.process(converted);
      acknowledgeProcessedMessage(msg, channel, deliveryTag);
    } catch (Exception e) {
      log.error("Processing failed: {}", e.getMessage());
      e.printStackTrace();

      T finalConverted = converted;
      optionalProcessor.ifPresent(eventProcessor ->
        eventProcessor.onError(e, finalConverted));

      log.error("Rejecting message {}", msg);
      rejectMessage(msg, channel, deliveryTag, e);
    }
  }

  public void rejectMessage(Message message, Channel channel, long deliveryTag, Exception e) {
    try {
      channel.basicNack(deliveryTag, REJECT_MULTIPLE_DELIVERIES, REQUEUE_REJECTED_MESSAGES);
      log.debug("NACK-end message {} because of exception {}", message, e.getMessage());
    } catch (IOException ioException) {
      log.error("Could not NACK message {} due to IOException {}", message, ioException.getMessage());
      ioException.printStackTrace();
    }
  }

  private boolean isMessageFromWaitQueue(String receivedRoutingKey) {
    return receivedRoutingKey.equals(topicConfig.getBindingKey());
  }

  private boolean isMessageFromParkingLotQueue(String receivedRoutingKey) {
    return receivedRoutingKey.equals(topicConfig.getWorkerQueueName());
  }

  private void handleExceededMaxRetry(Message message, Channel channel, long deliveryTag) {
    log.debug("MaxRetries exceeded.");
    shovelMessageToParkingLotQueue(channel, message, deliveryTag);
  }

  void acknowledgeProcessedMessage(
    Message message,
    Channel channel,
    long deliveryTag
  ) {
    try {
      log.debug(
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
        headerMap, topicConfig.getWaitQueueName()
      );
      if (isFromWaitQueue) {
        return hasExceededCount(headerMap);
      }
    }

    return false;
  }

  private boolean hasExceededCount(Map<String, ?> headerMap) {
    var count = (Long) headerMap.get("count");
    return count >= topicConfig.getMaxRetries();
  }

  private boolean isDeathHeaderFrom(Map<String, ?> headerMap, String queue) {
    var exchangeFromHeaderMap = (String) headerMap.get("exchange");
    var queueFromHeaderMap = (String) headerMap.get("queue");

    var areExchangesEqual = topicConfig.getExchange().equals(exchangeFromHeaderMap);
    var areQueuesEqual = queue.equals(queueFromHeaderMap);

    return areExchangesEqual && areQueuesEqual;
  }

  void shovelMessageToParkingLotQueue(Channel channel, Message failedMessage, long deliveryTag) {
    log.debug(
      "Shoveling message '{}' into queue '{}'",
      failedMessage,
      topicConfig.getParkingLotQueueName()
    );

    failedMessage.getMessageProperties()
      .setDeliveryMode(MessageDeliveryMode.PERSISTENT);

    this.rabbitTemplate.send(topicConfig.getParkingLotQueueName(), failedMessage);
    acknowledgeProcessedMessage(failedMessage, channel, deliveryTag);
  }

  Optional<String> extractRoutingKeyFromMessage(Message message, String receivedRoutingKey) {
    if (isMessageFromWaitQueue(receivedRoutingKey)) {
      return extractRoutingKeyFromMessageByQueue(message);
    } else if (isMessageFromParkingLotQueue(receivedRoutingKey)) {
      return extractRoutingKeyFromMessageByQueue(message);
    }

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
            isDeathHeaderFrom(headersMap, topicConfig.getWorkerQueueName())
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
