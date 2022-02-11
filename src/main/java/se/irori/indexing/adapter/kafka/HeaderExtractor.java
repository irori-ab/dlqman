package se.irori.indexing.adapter.kafka;

import io.vertx.mutiny.kafka.client.producer.KafkaHeader;
import java.util.List;
import lombok.Getter;

/**
 * Class that is responsible to extract 3rd party framework headers.
 *
 * Spring cloud: https://github.com/spring-cloud/spring-cloud-stream-binder-kafka/blob/main/spring-cloud-stream-binder-kafka/src/main/java/org/springframework/cloud/stream/binder/kafka/KafkaMessageChannelBinder.java
 *
 * Quarkus (Smallrye): https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/3.1/kafka/kafka.html#_failure_management
 */
@Getter
public class HeaderExtractor {

  private String topic;
  private String partition;
  private String offset;
  private String exceptionStackTrace;
  private String exceptionMessage;

  public HeaderExtractor(List<KafkaHeader>  headerList) {

  }
}
