package se.irori.persistence.model;

import static javax.persistence.CascadeType.ALL;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.common.constraint.NotNull;
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

  @NotNull
  private UUID sourceId;
  private Integer partition;

  @Column(name = "topic_offset")
  private Long offset;
  private byte[] payload;
  private String payloadString;
  private String classification;

  @OneToMany(mappedBy = "message", cascade = ALL, fetch = FetchType.EAGER)
  private List<MetadataDao> metadataList;

  public static MessageDao from(Message message) {
    return MessageDao.builder()
        .id(message.getId())
        .sourceId(message.getSourceId())
        .partition(message.getPartition())
        .offset(message.getOffset())
        .payload(message.getPayload())
        .payloadString(message.getPayloadString())
        .classification(message.getClassification())
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
        .partition(getPartition())
        .offset(getOffset())
        .payload(getPayload())
        .payloadString(getPayloadString())
        .classification(getClassification())
        .build();
  }
}
