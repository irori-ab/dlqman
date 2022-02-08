package se.irori.indexing.adapter.kafka;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KafkaAdapterConfiguration {
  private String bootstrapServers;
  private String groupId;
}
