package se.irori.persistence.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.logging.Log;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.mutiny.Uni;
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
@Entity(name = "Message")
@Table(name = "MESSAGE")
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
  @NamedQuery(name = "Message.toResend", query = "from Message where status = 'RESEND' and processAt < ?1"),
  @NamedQuery(name = "Message.resent", query = "update Message set status = 'RESENT' where id = ?1")
})
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

  @Transient
  private boolean persist;


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
    Message msg = Message.builder()
        .id(getId())
        .sourceId(sourceId)
      .sourceTopic(sourceTopic)
        .sourcePartition(sourcePartition)
        .sourceOffset(sourceOffset)
        .fingerprint(fingerprint)
//        .matchedRule(matchedRule)
      .destinationTopic(destinationTopic)
        .metadataList(metadataList
          .stream()
          .map(MetadataDao::to)
          .collect(Collectors.toList()))
        .build();
    return msg;
  }

  public static Uni<String> getTPO(MessageDao dao) {
    return Uni.createFrom().item(
      String.format("%s:%s:%s", dao.getSourceTopic(), dao.getSourcePartition(), dao.getSourceOffset()));
  }

  @ReactiveTransactional
  public static Uni<List<MessageDao>> toResend() {
    Log.trace("Fetching messages to resend");
    return find("#Message.toResend", OffsetDateTime.now()).<MessageDao>list();
  }

  @ReactiveTransactional
  public static Uni<String> setResent(MessageDao dao) {
    Log.debug(String.format("Updating status on message message [%s]", dao.id.toString()));
    return update("#Message.resent", dao.getId())
      .flatMap(updates -> MessageDao.getTPO(dao));
  }

  @ReactiveTransactional
  public static Uni<MessageDao> save(MessageDao dao) {
    if (dao.isPersist()) {
      Log.debug(String.format("Saving message [%s] TPO [%s:%s:%s]", dao.id.toString(), dao.getSourceTopic(),
        dao.getSourcePartition(), dao.getSourceOffset()));
      return persist(dao).map(empty -> dao);
    } else {
      Log.debug(String.format("Voiding message with TPO [%s:%s:%s]", dao.getSourceTopic(), dao.getSourcePartition(),
        dao.getSourceOffset()));
      return Uni.createFrom().item(dao);
    }
  }

}
