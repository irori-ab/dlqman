package se.irori.indexing.adapter.kafka;

import io.vertx.mutiny.kafka.client.producer.KafkaHeader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class that is responsible to extract 3rd party framework headers.
 * <p>
 * Spring cloud: https://github.com/spring-cloud/spring-cloud-stream-binder-kafka/blob/main/spring-cloud-stream-binder-kafka/src/main/java/org/springframework/cloud/stream/binder/kafka/KafkaMessageChannelBinder.java
 * <p>
 * Quarkus (Smallrye): https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/3.1/kafka/kafka.html#_failure_management
 * Connect: https://issues.apache.org/jira/browse/KAFKA-7003
 *
 */
@Slf4j
@Getter
public class HeaderExtractor {

  private final List<KafkaHeader> headerList;
  private String topic;
  private Integer partition;
  private Long offset;
  private String timestamp;
  private String timestampType;
  private String exceptionStackTrace;
  private String exceptionMessage;

  private Map<String, String> nonMatchedHeaders;
  private final static String SPRING_CLOUD_TOPIC = "X_ORIGINAL_TOPIC";
  private final static String SPRING_CLOUD_PARTITION = "X_ORIGINAL_PARTITION";
  private final static String SPRING_CLOUD_OFFSET = "X_ORIGINAL_OFFSET";
  private final static String SPRING_CLOUD_TIMESTAMP = "X_ORIGINAL_TIMESTAMP";
  private final static String SPRING_CLOUD_TIMESTAMP_TYPE = "X_ORIGINAL_TIMESTAMP_TYPE";

  private final static String SMALLRYE_REASON = "dead-letter-reason";
  private final static String SMALLRYE_CAUSE = "dead-letter-cause";
  private final static String SMALLRYE_TOPIC = "dead-letter-topic";
  private final static String SMALLRYE_PARTITION = "dead-letter-partition";
  private final static String SMALLRYE_OFFSET = "dead-letter-offset";

  private final static List<String> topicHeaders = List.of(SPRING_CLOUD_TOPIC, SMALLRYE_TOPIC);
  private final static List<String> partitionHeaders = List.of(SPRING_CLOUD_PARTITION, SMALLRYE_PARTITION);
  private final static List<String> offsetHeaders = List.of(SPRING_CLOUD_OFFSET, SMALLRYE_OFFSET);
  private final static List<String> timeStampHeaders = List.of(SPRING_CLOUD_TIMESTAMP);
  private final static List<String> timeStampTypeHeaders = List.of(SPRING_CLOUD_TIMESTAMP_TYPE);

  public HeaderExtractor(List<KafkaHeader> headerList) {
    this.headerList = headerList;
    nonMatchedHeaders = new HashMap<>();
    extract();
  }

  public HeaderExtractor extract() {
    headerList.forEach(header -> {
      if (topicHeaders.contains(header.key())) {
        topic = header.value().toString();
        return;
      }
      if (partitionHeaders.contains(header.key())) {
        partition = Integer.valueOf(header.value().toString());
        return;
      }
      if (offsetHeaders.contains(header.key())) {
        offset = Long.valueOf(header.value().toString());
        return;
      }
      if (timeStampHeaders.contains(header.key())) {
        timestamp = header.value().toString();
        return;
      }
      if (timeStampTypeHeaders.contains(header.key())) {
        timestamp = header.value().toString();
        return;
      }
      //If the header maps to a known DLQ type we add it to the "rest" map.
      nonMatchedHeaders.put(header.key(), header.value().toString());
    });
    return this;
  }

  public HeaderExtractor extract(String frameworkType) {
    return this;
  }
}
