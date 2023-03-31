package se.irori.ingestion.kafka;

import io.vertx.mutiny.kafka.client.producer.KafkaHeader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Class that is responsible to extract 3rd party framework headers.
 *
 * <p>Spring cloud: <a href="https://github.com/spring-cloud/spring-cloud-stream-binder-kafka/blob/main/spring-cloud-stream-binder-kafka/src/main/java/org/springframework/cloud/stream/binder/kafka/KafkaMessageChannelBinder.java">...</a>
 *
 * <p>Quarkus (Smallrye): <a href="https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/3.1/kafka/kafka.html#_failure_management">...</a>
 * Connect: <a href="https://issues.apache.org/jira/browse/KAFKA-7003">...</a>
 */
@Slf4j
@Getter
public class HeaderExtractor {

  public final static String SPRING_CLOUD_TOPIC = "x-original-topic";
  public final static String SPRING_CLOUD_PARTITION = "x-original-partition";
  public final static String SPRING_CLOUD_OFFSET = "x-original-offset";
  public final static String SPRING_CLOUD_TIMESTAMP = "x-original-timestamp";
  public final static String SPRING_CLOUD_TIMESTAMP_TYPE = "x-original-timestamp-type";
  public final static String SPRING_CLOUD_EXCEPTION_MESSAGE = "x-exception-message";
  public final static String SPRING_CLOUD_EXCEPTION_FQCN = "x-exception-fqcn";
  public final static String SPRING_CLOUD_EXCEPTION_STACKTRACE = "x-exception-stacktrace";

  public final static String SMALLRYE_REASON = "dead-letter-reason";
  public final static String SMALLRYE_CAUSE = "dead-letter-cause";
  public final static String SMALLRYE_TOPIC = "dead-letter-topic";
  public final static String SMALLRYE_PARTITION = "dead-letter-partition";
  public final static String SMALLRYE_OFFSET = "dead-letter-offset";

  public final static String INTERNAL_TOPIC = "internal-original-topic";
  public final static String INTERNAL_PARTITION = "internal-original-partition";
  public final static String INTERNAL_OFFSET = "internal-original-offset";
  public final static String INTERNAL_REASON = "internal-dlq-reason";
  public final static String INTERNAL_EXCEPTION_CLASS = "internal-dlq-exception-class";

  private final static List<String> topicHeaders = List.of(SPRING_CLOUD_TOPIC, SMALLRYE_TOPIC);
  private final static List<String> partitionHeaders = List.of(SPRING_CLOUD_PARTITION,
      SMALLRYE_PARTITION);
  private final static List<String> offsetHeaders = List.of(SPRING_CLOUD_OFFSET, SMALLRYE_OFFSET);
  private final static List<String> timeStampHeaders = List.of(SPRING_CLOUD_TIMESTAMP);
  private final static List<String> timeStampTypeHeaders = List.of(SPRING_CLOUD_TIMESTAMP_TYPE);

  private final static Map<String, String> headerMapper = Map.of(
    SPRING_CLOUD_TOPIC, INTERNAL_TOPIC,
    SPRING_CLOUD_OFFSET, INTERNAL_OFFSET,
    SPRING_CLOUD_PARTITION, INTERNAL_PARTITION,
    SPRING_CLOUD_EXCEPTION_FQCN, INTERNAL_EXCEPTION_CLASS,
    SPRING_CLOUD_EXCEPTION_MESSAGE, INTERNAL_REASON,
    SMALLRYE_REASON, INTERNAL_REASON,
    SMALLRYE_OFFSET, INTERNAL_OFFSET,
    SMALLRYE_TOPIC, INTERNAL_TOPIC,
    SMALLRYE_PARTITION, INTERNAL_PARTITION
  );

  private final List<KafkaHeader> headerList;

  private String topic;
  private Integer partition;
  private Long offset;
  private String timestamp;
  private String timestampType;
  private String exceptionStackTrace;
  private String exceptionMessage;

  private final Map<String, String> nonMatchedHeaders;
  private final Map<String, String> matchedHeaders;
  private final Map<String, String> allHeaders;

  public HeaderExtractor(List<KafkaHeader> headerList) {
    this.headerList = headerList;
    nonMatchedHeaders = new HashMap<>();
    allHeaders = new HashMap<>();
    matchedHeaders = new HashMap<>();
    extract();
  }

  public HeaderExtractor extract() {
    headerList.forEach(header -> {
      if (topicHeaders.contains(header.key())) {
        topic = header.value().toString();
        matchedHeaders.put(header.key(), header.value().toString());
        return;
      }
      if (partitionHeaders.contains(header.key())) {
        partition = Integer.valueOf(header.value().toString());
        matchedHeaders.put(header.key(), header.value().toString());
        return;
      }
      if (offsetHeaders.contains(header.key())) {
        offset = Long.valueOf(header.value().toString());
        matchedHeaders.put(header.key(), header.value().toString());
        return;
      }
      if (timeStampHeaders.contains(header.key())) {
        timestamp = header.value().toString();
        matchedHeaders.put(header.key(), header.value().toString());
        return;
      }
      if (timeStampTypeHeaders.contains(header.key())) {
        timestamp = header.value().toString();
        matchedHeaders.put(header.key(), header.value().toString());
        return;
      }
      //If the header does not map to a known DLQ type we add it to the "rest" map.
      nonMatchedHeaders.put(header.key(), header.value().toString());
    });
    allHeaders.putAll(nonMatchedHeaders);
    allHeaders.putAll(matchedHeaders);
    return this;
  }

  public static Stream<Metadata> convertHeader(String key, String value) {
    if (headerMapper.containsKey(key)) {
      return Stream.of(sourceKeyValue(key, value), internalKeyValue(key, value));
    }
    return Stream.of(sourceKeyValue(key, value));
  }

  private static Metadata sourceKeyValue(String key, String value) {
    return Metadata.builder()
      .id(UUID.randomUUID())
      .type(MetaDataType.SOURCE_HEADER)
      .key(key)
      .value(value)
      .build();
  }
  private static Metadata internalKeyValue(String key, String value) {
    return Metadata.builder()
      .id(UUID.randomUUID())
      .type(MetaDataType.INTERNAL)
      .key(headerMapper.get(key))
      .value(value)
      .build();
  }
}
