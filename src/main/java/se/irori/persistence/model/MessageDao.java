package se.irori.persistence.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.common.constraint.NotNull;
import lombok.*;
import se.irori.model.Message;
import se.irori.model.MessageStatus;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static javax.persistence.CascadeType.ALL;

@Getter
@Setter
@Builder
@Entity(name = "MessageDao")
@Table(name = "MESSAGE")
@NoArgsConstructor
@AllArgsConstructor
public class MessageDao extends PanacheEntityBase {

  @Id
  @NotNull
  public UUID id;

  private String sourceId;
  private String sourceTopic;
  private Integer sourcePartition;

  private Long sourceOffset;

  private String fingerprint;

  private String destinationTopic;

  @Enumerated(EnumType.STRING)
  private MessageStatus status;
  private String matchedRule;

  private OffsetDateTime processAt;

  private Long waitTime;


  @OneToMany(mappedBy = "message", cascade = ALL, fetch = FetchType.EAGER)
  public List<MetadataDao> metadataList;

  public static MessageDao from(Message message) {
    return MessageDao.builder()
        .id(message.getId())
        .sourceId(message.getSourceId())
        .sourcePartition(message.getSourcePartition())
        .sourceOffset(message.getSourceOffset())
        .sourceTopic(message.getSourceTopic())
        .destinationTopic(message.getDestinationTopic())
        .fingerprint(message.getFingerprint())
        .status(message.getStatus())
        .matchedRule(message.getMatchedRule() != null ? message.getMatchedRule().name() : null)
        .metadataList(message.getMetadataList()
            .stream()
            .map(MetadataDao::from)
            .collect(Collectors.toList()))
        .build();
  }


  public Message toMessage() {
    return Message.builder()
        .id(getId())
        .sourceId(getSourceId())
        .sourcePartition(getSourcePartition())
        .sourceOffset(getSourceOffset())
        .fingerprint(getFingerprint())
        //.matchedRule(getMatchedRule())
        .metadataList(getMetadataList()
          .stream()
          .map(MetadataDao::to)
          .collect(Collectors.toList()))
          .build();
  }

  public String getIdentifier() {
    return String.format("%s:%s:%s", getSourceTopic(), getSourcePartition(), getSourceOffset());
  }
}
