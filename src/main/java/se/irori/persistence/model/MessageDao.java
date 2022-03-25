package se.irori.persistence.model;

import static javax.persistence.CascadeType.ALL;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.mutiny.Uni;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.irori.model.Message;
import se.irori.model.MessageStatus;
import se.irori.model.TimestampType;

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

  private LocalDateTime timeStamp;
  private TimestampType timeStampType;

  private LocalDateTime indexTime;

  @NotNull
  private UUID sourceId;
  private Integer partition;

  @Column(name = "topic_offset")
  private Long offset;
  private byte[] payload;
  private String payloadString;
  private MessageStatus messageStatus;

  @OneToMany(mappedBy = "message", cascade = ALL, fetch = FetchType.LAZY)
  private List<MetadataDao> metadataList;

  public static Uni<List<MessageDao>> listMessages(
      LocalDateTime startTime,
      LocalDateTime endTime,
      UUID sourceId) {
    if(startTime != null & endTime != null) {
      return MessageDao.<MessageDao>find("indexTime < ?1 and indexTime > ?2", startTime, endTime)
          .list();
    }
    return MessageDao.<MessageDao>find("sourceId = ?1", sourceId).list();
  }

  public static MessageDao from(Message message) {
    return MessageDao.builder()
        .id(message.getId())
        .timeStamp(message.getTimeStamp())
        .timeStampType(message.getTimeStampType())
        .indexTime(message.getIndexTime())
        .sourceId(message.getSourceId())
        .partition(message.getPartition())
        .offset(message.getOffset())
        .payload(message.getPayload())
        .payloadString(message.getPayloadString())
        .messageStatus(message.getStatus())
        .metadataList(message.getMetadataList()
            .stream()
            .map(MetadataDao::from)
            .collect(Collectors.toList()))
        .build();
  }


  public Message toMessage() {
    return Message.builder()
        .id(getId())
        .timeStamp(getTimeStamp())
        .timeStampType(getTimeStampType())
        .indexTime(getIndexTime())
        .sourceId(getSourceId())
        .partition(getPartition())
        .offset(getOffset())
        .payload(getPayload())
        .payloadString(getPayloadString())
        .status(getMessageStatus())
        .build();
  }
}
