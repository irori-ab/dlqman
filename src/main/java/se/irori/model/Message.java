package se.irori.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.irori.config.Rule;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

  private UUID id = UUID.randomUUID();
  private String sourceId;
  private String sourceTopic;
  private Integer sourcePartition;

  private Long sourceOffset;

  private String fingerprint;

  private MessageStatus status;

  private String destinationTopic;
  private Rule matchedRule;

  private List<Metadata> metadataList;
}
