package se.irori.model;

import static javax.persistence.CascadeType.ALL;

import io.smallrye.common.constraint.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

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
  private MessageStatus status;

  @OneToMany(mappedBy = "message", cascade = ALL, fetch = FetchType.EAGER)
  private List<Metadata> metadataList;
}
