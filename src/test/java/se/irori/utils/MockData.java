package se.irori.utils;

import se.irori.ingestion.kafka.HeaderExtractor;
import se.irori.model.Message;
import se.irori.model.MetaDataType;
import se.irori.model.Metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MockData {
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
}
