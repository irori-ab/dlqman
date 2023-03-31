package se.irori.utils;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeader;
import se.irori.config.Rule;
import se.irori.config.Source;
import se.irori.ingestion.kafka.HeaderExtractor;
import se.irori.model.Message;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

public final class MockHelper {
  public static List<Metadata> getDefaultSpringHeaders() {
    List<Metadata> list = new ArrayList<>();
    list.add(Metadata.builder()
      .key(HeaderExtractor.SPRING_CLOUD_TOPIC)
      .value("original-topic")
      .type(MetaDataType.SOURCE_HEADER).build());
    list.add(Metadata.builder()
      .key(HeaderExtractor.SPRING_CLOUD_OFFSET)
      .value("1000")
      .type(MetaDataType.SOURCE_HEADER).build());
    list.add(Metadata.builder()
      .key(HeaderExtractor.SPRING_CLOUD_PARTITION)
      .value("0")
      .type(MetaDataType.SOURCE_HEADER).build());
    list.add(Metadata.builder()
      .key(HeaderExtractor.SPRING_CLOUD_EXCEPTION_FQCN)
      .value(NullPointerException.class.getName())
      .type(MetaDataType.SOURCE_HEADER).build());

    return list;
  }

  public static Message getDefaultSpringMessage(Metadata addition) {
    List<Metadata> list = new ArrayList<>(getDefaultSpringHeaders());
    if (addition != null) {
      list.add(addition);
    }
    return Message.builder()
      .id(UUID.randomUUID())
      .metadataList(list)
      .build();
  }

  public static org.eclipse.microprofile.reactive.messaging.Message generateMessage(String key, Object value, RecordHeader... headers) {

    return org.eclipse.microprofile.reactive.messaging.Message.of(value, org.eclipse.microprofile.reactive.messaging.Metadata.of(OutgoingKafkaRecordMetadata.<String>builder()
      .withKey(key)
      .withHeaders(List.of(headers))
      .build()));
  }


  public static Source source() {
    return SourceImpl.builder()
      .sourceTopic("source-topic")
      .name("testsource")
      .resendTopic("destination-topic")
      .matchRules(List.of(RuleImpl.builder()
        .name("regexRule")
        .priority(5)
        .matcher("regextest")
        .strategy("dismiss")
        .build())
      )
      .build();
  }

  public static void mockDefaultSource(Source mock, Rule... rules) {
    when(mock.name()).thenReturn("testsource");
    when(mock.sourceTopic()).thenReturn("source-topic");
    when(mock.resendTopic()).thenReturn("destination-topic");
    when(mock.matchRules()).thenReturn(List.of(rules));
  }

  public static void mockDefaultRegexRule(Rule mock, String name, int prio, String matcher) {
    when(mock.name()).thenReturn(name);
    when(mock.priority()).thenReturn(prio);
    when(mock.matcher()).thenReturn(matcher);
    when(mock.strategy()).thenReturn("dismiss");
  }

  public static void mockDefaultAllRule(Rule mock) {
    when(mock.name()).thenReturn("all");
    when(mock.priority()).thenReturn(100);
    when(mock.matcher()).thenReturn("all");
    when(mock.strategy()).thenReturn("dismiss");
  }

  public static Message.MessageBuilder messageBuilder(Map<String, String> headers) {
    return Message.builder()
      .id(UUID.randomUUID())
      .sourceTopic("test-source")
      .sourcePartition(0)
      .sourceOffset(0l)
      .sourceId(source().name())
      .metadataList(headers.entrySet().stream().map(entry ->
        Metadata.builder().key(entry.getKey()).value(entry.getValue()).build()).collect(Collectors.toList())
      );
  }
}
