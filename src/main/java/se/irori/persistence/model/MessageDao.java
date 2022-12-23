package se.irori.persistence.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.common.constraint.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.irori.model.Message;
import se.irori.model.MessageStatus;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static javax.persistence.CascadeType.ALL;

@Getter
@Builder
@Entity
@Table(name = "message")
@NoArgsConstructor
@AllArgsConstructor
public class MessageDao extends PanacheEntityBase {

  @Id
  @NotNull
  private UUID id;

  private String sourceId;
  private String sourceTopic;
  private Integer sourcePartition;
  @Column(name = "topic_offset")
  private Long sourceOffset;

  private String fingerprint;

  private MessageStatus status;
  private String matchedRule;


  @OneToMany(mappedBy = "message", cascade = ALL, fetch = FetchType.EAGER)
  private List<MetadataDao> metadataList;

  public static MessageDao from(Message message) {
    return MessageDao.builder()
        .id(message.getId())
        .sourceId(message.getSourceId())
        .sourcePartition(message.getSourcePartition())
        .sourceOffset(message.getSourceOffset())
        .fingerprint(message.getFingerprint())
        .matchedRule(message.getMatchedRule())
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
        .matchedRule(getMatchedRule())
        .metadataList(getMetadataList()
          .stream()
          .map(MetadataDao::to)
          .collect(Collectors.toList()))
          .build();
  }
}
